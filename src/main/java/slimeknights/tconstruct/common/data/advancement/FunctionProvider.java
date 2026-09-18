package slimeknights.tconstruct.common.data.advancement;

import com.google.common.hash.Hashing;
import net.minecraft.util.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.tconstruct.TConstruct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Writes mcfunctions that grant vanilla advancements from Tinkers tools. */
public class FunctionProvider implements DataProvider {
  private final PackOutput.PathProvider pathProvider;
  private final Map<Identifier,String> functions = new HashMap<>();

  public FunctionProvider(PackOutput output) {
    this.pathProvider = output.createPathProvider(Target.DATA_PACK, "function");
  }

  private void addAdvancements() {
    grant(AdvancementIds.STORY_ROOT);
    grant(AdvancementIds.STONE_PICK);
    grant(AdvancementIds.IRON_PICK);
    grant(AdvancementIds.NETHERITE_HOE);
    grant(AdvancementIds.WALK_ON_POWDER_SNOW);
    grant(AdvancementIds.OBTAIN_ARMOR);
    grant(AdvancementIds.SHINY_GEAR);
    grant(AdvancementIds.NETHERITE_ARMOR);
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    addAdvancements();
    return GenericDataProvider.allOf(functions.entrySet().stream().map(entry -> saveString(cache, entry.getKey(), entry.getValue())));
  }

  private void grant(Identifier advancement) {
    functions.put(AdvancementIds.function(advancement), "advancement grant @s only " + advancement + '\n');
  }

  private CompletableFuture<?> saveString(CachedOutput cache, Identifier location, String data) {
    return CompletableFuture.runAsync(() -> {
      try {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        cache.writeIfNeeded(this.pathProvider.file(location, "mcfunction"), bytes, Hashing.sha1().hashBytes(bytes));
      } catch (IOException e) {
        TConstruct.LOG.error("Couldn't write function {}", location, e);
        throw new CompletionException(e);
      }
    }, Util.backgroundExecutor());
  }

  @Override
  public String getName() {
    return "Continuum Construct advancement mcfunction provider";
  }
}
