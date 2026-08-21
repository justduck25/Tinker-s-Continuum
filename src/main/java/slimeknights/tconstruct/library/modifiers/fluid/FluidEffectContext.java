package slimeknights.tconstruct.library.modifiers.fluid;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Objects;

import static slimeknights.tconstruct.library.tools.helper.ModifierUtil.asLiving;
import static slimeknights.tconstruct.library.tools.helper.ModifierUtil.asPlayer;

/** Context for calling fluid effects */
public abstract class FluidEffectContext {
  protected final Level level;
  /** Entity using the fluid */
  @Nullable
  protected final LivingEntity entity;
  /** Player using the fluid, may be null if a non-player is the source of the fluid */
  @Nullable
  protected final Player player;
  /** Projectile that caused the fluid, null if no projectile is used (e.g. melee or interact effects) */
  @Nullable
  protected final Projectile projectile;
  /** Item stack fallback for when the entity is not set */
  protected final ItemStack stack;

  protected FluidEffectContext(Level level, @Nullable LivingEntity entity, @Nullable Player player, @Nullable Projectile projectile, ItemStack stack) {
    this.level = level;
    this.entity = entity;
    this.player = player;
    this.projectile = projectile;
    this.stack = stack;
  }

  public Level getLevel() {
    return level;
  }

  @Nullable
  public LivingEntity getEntity() {
    return entity;
  }

  @Nullable
  public Player getPlayer() {
    return player;
  }

  @Nullable
  public Projectile getProjectile() {
    return projectile;
  }

  public ItemStack getStack() {
    return stack;
  }

  /** @deprecated use {@link #FluidEffectContext(Level,LivingEntity,Player,Projectile,ItemStack} */
  @Deprecated
  public FluidEffectContext(Level level, @Nullable LivingEntity entity, @Nullable Player player, @Nullable Projectile projectile) {
    this(level, entity, player, projectile, ItemStack.EMPTY);
  }

  /** Gets the relevant block position for this context */
  public abstract BlockPos getBlockPos();

  /** Gets the position where the fluid hit on the block or entity */
  public abstract Vec3 getLocation();

  /** Gets a damage source based on this context */
  public DamageSource createDamageSource() {
    if (projectile != null) {
      return projectile.damageSources().mobProjectile(projectile, entity);
    }
    if (player != null) {
      return player.damageSources().playerAttack(player);
    }
    if (entity != null) {
      return entity.damageSources().mobAttack(entity);
    }
    // we should never reach here, but just in case
    return level.damageSources().generic();
  }

  /** Gets the direct source of damage for this context. This prefers the projectile over the entity. */
  @Nullable
  public net.minecraft.world.entity.Entity getDirectSource() {
    if (projectile != null) {
      return projectile;
    }
    return entity;
  }

  /**
   * Gets the cause of effects caused by this context.
   * @see Projectile#getEffectSource()
   */
  @Nullable
  public net.minecraft.world.entity.Entity getEffectSource() {
    // effects prefer the mob, but accept the projectile if nothing else
    if (entity != null) {
      return entity;
    }
    return projectile;
  }

  /** If true, this context is not allowed to break blocks at the given position */
  public boolean breakRestricted() {
    // TODO: consider whether its worth fetching break tags from the player's held item. Problem is context doesn't know which hand triggered this, and via projectiles it may change
    // TODO: do we need to check the client game mode?
    return player != null && !player.getAbilities().mayBuild && player instanceof ServerPlayer serverPlayer && serverPlayer.gameMode.getGameModeForPlayer() == GameType.ADVENTURE;
  }

  /** If true, this context is not allowed to place blocks at the given position */
  public boolean placeRestricted(ItemStack stack) {
    if (player == null || player.getAbilities().mayBuild) return false;
    AdventureModePredicate predicate = stack.get(DataComponents.CAN_PLACE_ON);
    return predicate == null || !predicate.test(new BlockInWorld(level, getBlockPos(), false));
  }

  /** Context for fluid effects targeting an entity */
  public static class Entity extends FluidEffectContext {
    private final net.minecraft.world.entity.Entity target;
    @Nullable
    private final LivingEntity livingTarget;
    private final Vec3 location;

    public net.minecraft.world.entity.Entity getTarget() {
      return target;
    }

    @Nullable
    public LivingEntity getLivingTarget() {
      return livingTarget;
    }

    public Vec3 getLocation() {
      return location;
    }

    private Entity(Level level, @Nullable LivingEntity holder, @Nullable Player player, @Nullable Projectile projectile, ItemStack stack, net.minecraft.world.entity.Entity target, @Nullable LivingEntity livingTarget, @Nullable Vec3 location) {
      super(level, holder, player, projectile, stack);
      this.target = target;
      this.livingTarget = livingTarget;
      this.location = Objects.requireNonNullElse(location, target.position());
    }

    /** @deprecated use {@link #builder(Level)} */
    @Deprecated(forRemoval = true)
    public Entity(Level level, @Nullable LivingEntity holder, @Nullable Player player, @Nullable Projectile projectile, net.minecraft.world.entity.Entity target, @Nullable LivingEntity livingTarget, @Nullable Vec3 location) {
      this(level, holder, player, projectile, ItemStack.EMPTY, target, livingTarget, location);
    }

    /** @deprecated use {@link #builder(Level)} */
    @Deprecated(forRemoval = true)
    public Entity(Level level, @Nullable LivingEntity holder, @Nullable Player player, @Nullable Projectile projectile, net.minecraft.world.entity.Entity target, @Nullable LivingEntity livingTarget) {
      this(level, holder, player, projectile, target, livingTarget, null);
    }

    /** @deprecated use {@link #builder(Level)} */
    @Deprecated(forRemoval = true)
    public Entity(Level level, @Nullable LivingEntity holder, @Nullable Projectile projectile, net.minecraft.world.entity.Entity target, @Nullable Vec3 location) {
      this(level, holder, asPlayer(holder), projectile, target, asLiving(target), location);
    }

    /** @deprecated use {@link #builder(Level)} */
    @Deprecated(forRemoval = true)
    public Entity(Level level, Player player, @Nullable Projectile projectile, LivingEntity target) {
      this(level, player, player, projectile, target, target);
    }

    @Override
    public BlockPos getBlockPos() {
      return target.blockPosition();
    }
  }

  /** Context for fluid effects targeting an entity */
  public static class Block extends FluidEffectContext {
    private final BlockHitResult hitResult;

    public BlockHitResult getHitResult() {
      return hitResult;
    }
    private BlockState state;

    private Block(Level level, @Nullable LivingEntity holder, @Nullable Player player, @Nullable Projectile projectile, ItemStack stack, BlockHitResult hitResult) {
      super(level, holder, player, projectile, stack);
      this.hitResult = hitResult;
    }

    /** @deprecated use {@link #builder(Level)} */
    @Deprecated(forRemoval = true)
    public Block(Level level, @Nullable LivingEntity holder, @Nullable Player player, @Nullable Projectile projectile, BlockHitResult hitResult) {
      this(level, holder, player, projectile, ItemStack.EMPTY, hitResult);
    }

    /** @deprecated use {@link #builder(Level)} */
    @Deprecated(forRemoval = true)
    public Block(Level level, @Nullable LivingEntity holder, @Nullable Projectile projectile, BlockHitResult hitResult) {
      this(level, holder, asPlayer(holder), projectile, hitResult);
    }

    /** @deprecated use {@link #builder(Level)} */
    @Deprecated(forRemoval = true)
    public Block(Level level, @Nullable Player player, @Nullable Projectile projectile, BlockHitResult hitResult) {
      this(level, player, player, projectile, hitResult);
    }

    @Override
    public BlockPos getBlockPos() {
      return hitResult.getBlockPos();
    }

    @Override
    public Vec3 getLocation() {
      return hitResult.getLocation();
    }

    /** Creates a copy of this context with the given hit result */
    public Block withHitResult(BlockHitResult result) {
      return new Block(level, entity, player, projectile, stack, result);
    }

    /** Gets the block state targeted by this context */
    public BlockState getBlockState() {
      if (state == null) {
        state = level.getBlockState(hitResult.getBlockPos());
      }
      return state;
    }

    /** Checks if the block in front of the hit block is replaceable */
    public boolean isOffsetReplaceable() {
      return level.getBlockState(hitResult.getBlockPos().relative(hitResult.getDirection())).canBeReplaced();
    }
  }

  /* Builder */

  /** Creates a new builder instance */
  public static Builder builder(Level level) {
    return new Builder(level);
  }

  @CanIgnoreReturnValue
  public static class Builder {
    /** Context level */
    private final Level level;
    private LivingEntity entity = null;
    private Player player = null;
    /** Projectile using the fluid */
    private Projectile projectile = null;
    /** Item stack fallback for when the entity is not set */
    private ItemStack stack = ItemStack.EMPTY;
    /** Location we hit the entity. Ignored for blocks */
    private Vec3 location = null;

    private Builder(Level level) {
      this.level = level;
    }

    public Builder projectile(Projectile projectile) {
      this.projectile = projectile;
      return this;
    }

    public Builder stack(ItemStack stack) {
      this.stack = stack;
      return this;
    }

    public Builder location(Vec3 location) {
      this.location = location;
      return this;
    }

    /** Set the entity and player using the fluid */
    public Builder user(@Nullable LivingEntity entity, @Nullable Player player) {
      this.entity = entity;
      this.player = player;
      return this;
    }

    /** Set the entity using the fluid, setting the player by instanceof check */
    public Builder user(@Nullable net.minecraft.world.entity.Entity entity) {
      return user(asLiving(entity));
    }

    /** Set the entity using the fluid, setting the player by instanceof check */
    public Builder user(@Nullable LivingEntity entity) {
      if (entity != null) {
        user(entity, asPlayer(entity));
      }
      return this;
    }

    /** Set the player using the fluid */
    public Builder user(@Nullable Player player) {
      return user(player, player);
    }

    /** Creates a block context */
    public Block block(BlockHitResult hitResult) {
      return new Block(level, entity, player, projectile, stack, hitResult);
    }

    /** Creates an entity context */
    public Entity target(net.minecraft.world.entity.Entity target, @Nullable LivingEntity livingTarget) {
      return new Entity(level, entity, player, projectile, stack, target, livingTarget, location);
    }

    /** Creates an entity context */
    public Entity target(net.minecraft.world.entity.Entity target) {
      return target(target, asLiving(target));
    }

    /** Creates an entity context */
    public Entity target(LivingEntity target) {
      return target(target, target);
    }
  }
}
