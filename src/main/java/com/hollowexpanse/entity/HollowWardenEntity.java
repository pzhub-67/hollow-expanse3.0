package com.hollowexpanse.entity;

import com.hollowexpanse.Config;
import com.hollowexpanse.registry.ModBlocks;
import com.hollowexpanse.registry.ModEnchantments;
import com.hollowexpanse.registry.ModEntities;
import java.util.ArrayDeque;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The Hollow Warden.
 * Phase 1 (100-66%): melee + shard barrages.
 * Phase 2 (66-33%):  gravity wells pull you in (Gravity Anchor resists); the arena rim crumbles.
 * Phase 3 (below 33%): the arena fragments into small platforms; the Warden vanishes and strikes from nowhere.
 */
public class HollowWardenEntity extends Monster {
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private BlockPos home = BlockPos.ZERO;
    private boolean hasHome = false;
    private int phase = 1;
    private int shardCooldown = 80;
    private int wellCooldown = 160;
    private int wellTicks = 0;
    private int strikeCooldown = 140;
    private int vanishTicks = 0;
    private final ArrayDeque<BlockPos> crumbleQueue = new ArrayDeque<>();

    public HollowWardenEntity(EntityType<? extends HollowWardenEntity> type, Level level) {
        super(type, level);
        this.xpReward = 300;
        Objects.requireNonNull(getAttribute(Attributes.MAX_HEALTH)).setBaseValue(Config.wardenHealth());
        this.setHealth(this.getMaxHealth());
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 320.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    public void setHome(BlockPos pos) {
        this.home = pos.immutable();
        this.hasHome = true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ------------------------------------------------------------------ AI

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel serverLevel)) return;
        if (!hasHome) setHome(blockPosition());

        bossEvent.setProgress(getHealth() / getMaxHealth());
        updatePhase();

        if (getY() < home.getY() - 24) {          // never fall off our own arena
            this.moveTo(home.getX() + 0.5D, home.getY() + 1.0D, home.getZ() + 0.5D);
            this.getNavigation().stop();
        }

        LivingEntity target = getTarget();
        if (target != null && target.isAlive()) {
            if (--shardCooldown <= 0) {
                shardCooldown = phase == 1 ? 70 : 45;
                fireShards(serverLevel, target);
            }
            if (phase >= 2) {
                tickGravityWell(serverLevel);
            }
            if (phase == 3) {
                tickVanishStrike(serverLevel, target);
            }
        }
        processCrumble(serverLevel);
        if (tickCount % 3 == 0) orbitShards(serverLevel);
    }

    private void updatePhase() {
        float fraction = getHealth() / getMaxHealth();
        int newPhase = fraction > 0.66F ? 1 : fraction > 0.33F ? 2 : 3;
        if (newPhase != phase) {
            phase = newPhase;
            this.playSound(SoundEvents.WARDEN_ROAR, 4.0F, 0.5F);
            wellCooldown = 60;
            queueCrumble(newPhase);
            for (Player p : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(48.0D))) {
                p.displayClientMessage(Component.translatable("message.hollow_expanse.warden_phase_" + newPhase), true);
            }
        }
    }

    private void fireShards(ServerLevel level, LivingEntity target) {
        int count = phase == 1 ? 3 : 5;
        Vec3 origin = position().add(0.0D, 2.6D, 0.0D);
        Vec3 aim = target.getEyePosition().subtract(origin);
        Vec3 side = new Vec3(-aim.z, 0.0D, aim.x);
        side = side.lengthSqr() > 1.0E-4D ? side.normalize() : Vec3.ZERO;
        for (int i = 0; i < count; i++) {
            double offset = (i - (count - 1) / 2.0D) * 0.12D * Math.max(2.0D, aim.length() * 0.15D);
            VoidShardEntity shard = new VoidShardEntity(ModEntities.VOID_SHARD.get(), this, level);
            shard.setPos(origin.x, origin.y, origin.z);
            shard.shoot(aim.x + side.x * offset, aim.y + 0.1D, aim.z + side.z * offset, 1.2F, 0.6F);
            level.addFreshEntity(shard);
        }
        this.playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 2.0F, 0.8F);
    }

    /** Pulls everything toward the Warden for ~3.5s. Gravity Anchor + sneaking fights it. */
    private void tickGravityWell(ServerLevel level) {
        if (wellTicks > 0) {
            wellTicks--;
            Vec3 center = position().add(0.0D, 1.5D, 0.0D);
            for (Player p : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(26.0D),
                    pl -> pl.isAlive() && !pl.isSpectator() && !pl.isCreative())) {
                Vec3 toward = center.subtract(p.position());
                if (toward.length() < 3.0D) continue;
                double resist = ModEnchantments.gravityResistance(p);
                Vec3 pull = toward.normalize().scale(0.17D * (1.0D - resist));
                p.setDeltaMovement(p.getDeltaMovement().add(pull));
                p.hurtMarked = true;
            }
            if (tickCount % 2 == 0) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1.5D, getZ(), 40, 6.0D, 3.0D, 6.0D, 0.2D);
            }
        } else if (--wellCooldown <= 0) {
            wellCooldown = phase == 2 ? 220 : 160;
            wellTicks = 70;
            this.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 0.6F);
        }
    }

    /** Phase 3: fade away, then reappear beside the target and hit hard. */
    private void tickVanishStrike(ServerLevel level, LivingEntity target) {
        if (vanishTicks > 0) {
            vanishTicks--;
            level.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.5D, getZ(), 6, 0.6D, 1.2D, 0.6D, 0.01D);
            if (vanishTicks == 0) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                this.moveTo(target.getX() + Math.cos(angle) * 2.5D, target.getY(), target.getZ() + Math.sin(angle) * 2.5D);
                this.getNavigation().stop();
                setInvisible(false);
                setInvulnerable(false);
                this.doHurtTarget(target);
                this.playSound(SoundEvents.WARDEN_ATTACK_IMPACT, 3.0F, 0.5F);
                level.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getY() + 1.5D, getZ(), 1, 0, 0, 0, 0);
            }
        } else if (--strikeCooldown <= 0) {
            strikeCooldown = 150;
            vanishTicks = 35;
            setInvisible(true);
            setInvulnerable(true);
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 3.0F, 0.4F);
        }
    }

    private void orbitShards(ServerLevel level) {
        double base = tickCount * 0.12D;
        for (int i = 0; i < 5; i++) {
            double a = base + i * (Math.PI * 2.0D / 5.0D);
            level.sendParticles(ParticleTypes.END_ROD,
                    getX() + Math.cos(a) * 2.3D, getY() + 1.6D + Math.sin(a * 2.0D) * 0.5D, getZ() + Math.sin(a) * 2.3D,
                    1, 0, 0, 0, 0);
        }
    }

    // ------------------------------------------------------------------ crumbling arena

    private void queueCrumble(int newPhase) {
        if (!Config.crumble() || newPhase < 2) return;
        int cx = home.getX(), cz = home.getZ(), top = home.getY() + 2;
        int radius = newPhase == 2 ? 28 : 18;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                boolean remove = newPhase == 2
                        ? d > 17.0D
                        : d > 5.0D && d <= 17.0D && Math.floorMod(Math.floorDiv(dx, 4) + Math.floorDiv(dz, 4), 2) == 0;
                if (!remove) continue;
                for (int y = top; y >= top - 56; y--) {
                    crumbleQueue.add(new BlockPos(cx + dx, y, cz + dz));
                }
            }
        }
    }

    private static boolean isArenaBlock(BlockState state) {
        return state.is(ModBlocks.HOLLOW_STONE.get()) || state.is(ModBlocks.HOLLOW_SOIL.get())
                || state.is(ModBlocks.VOIDMOSS.get()) || state.is(ModBlocks.VOIDSTEEL_ORE.get())
                || state.is(ModBlocks.LUMEN_BLOOM.get());
    }

    private void processCrumble(ServerLevel level) {
        int steps = 0, broken = 0;
        while (!crumbleQueue.isEmpty() && steps < 2500 && broken < 260) {
            BlockPos pos = crumbleQueue.poll();
            steps++;
            if (!level.isLoaded(pos)) continue;
            if (isArenaBlock(level.getBlockState(pos))) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                broken++;
                if ((pos.getX() + pos.getY() + pos.getZ()) % 41 == 0) {
                    level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                            3, 0.3D, 0.3D, 0.3D, 0.02D);
                }
            }
        }
        if (broken > 100 && tickCount % 4 == 0) {
            this.playSound(SoundEvents.DEEPSLATE_BREAK, 4.0F, 0.6F);
        }
    }

    // ------------------------------------------------------------------ boss bar, save data, sounds

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HomeX", home.getX());
        tag.putInt("HomeY", home.getY());
        tag.putInt("HomeZ", home.getZ());
        tag.putBoolean("HasHome", hasHome);
        tag.putInt("Phase", phase);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        home = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
        hasHome = tag.getBoolean("HasHome");
        phase = Math.max(1, tag.getInt("Phase"));
        if (hasCustomName()) bossEvent.setName(getDisplayName());
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return 0.6F;
    }
}
