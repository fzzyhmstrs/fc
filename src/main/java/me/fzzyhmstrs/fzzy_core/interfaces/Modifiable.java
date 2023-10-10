package me.fzzyhmstrs.fzzy_core.interfaces;

import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

public interface Modifiable {

    default List<Identifier> defaultModifiers(ModifierHelperType<?> type){return Collections.emptyList();}

    default void addModifierTooltip(ItemStack stack, List<Text> tooltip, TooltipContext context, ModifierHelperType<?> type){
        type.getModifierInitializer().addModifierTooltip(stack, tooltip, context);
    }

    default boolean canBeModifiedBy(ModifierHelperType<?> type){
        return true;
    }

    default Identifier modifierObjectPredicate(ItemStack stack){
        return AbstractModifierHelper.Companion.BLANK;
    }
}
