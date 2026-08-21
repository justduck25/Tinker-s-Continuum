package slimeknights.tconstruct.shared.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.command.MantleCommand;
import slimeknights.tconstruct.TConstruct;

/** Command to generate missing melting recipes. TODO: port recipe scanning to NeoForge 26 RecipeHolder/Fluid APIs. */
public class GenerateMeltingRecipesCommand {
  public static final Identifier MELTING_CONFIGURATION = TConstruct.getResource("melting_recipe_generation");
  private static final Component DISABLED = TConstruct.makeTranslation("command", "generate_melting_recipes.disabled");

  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand, CommandBuildContext context) {
    register(subCommand);
  }

  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(sender -> MantleCommand.hasPermission(sender, MantleCommand.PERMISSION_GAME_COMMANDS))
              .executes(GenerateMeltingRecipesCommand::run);
  }

  private static int run(CommandContext<CommandSourceStack> context) {
    context.getSource().sendSuccess(() -> DISABLED, false);
    return 0;
  }
}