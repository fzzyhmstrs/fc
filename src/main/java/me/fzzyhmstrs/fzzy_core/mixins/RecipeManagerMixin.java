package me.fzzyhmstrs.fzzy_core.mixins;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import me.fzzyhmstrs.fzzy_core.recipe.OptionalRecipeLoader;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Shadow
    private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;

    @Shadow
    private Map<Identifier, Recipe<?>> recipesById;

    @Final
    @Shadow
    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At(value = "RETURN"))
    private void fzzy_core_applyOptionalRecipes(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci){
        LOGGER.info("Starting application of optional recipes");
        recipes = OptionalRecipeLoader.INSTANCE.provideRecipeMap(recipes);
        recipesById = OptionalRecipeLoader.INSTANCE.provideRecipesByIdMap(recipesById);
        LOGGER.info("Finished application of optional recipes");
    }

}
