package me.fzzyhmstrs.fzzy_core.config_util.validated_field

import me.fzzyhmstrs.fzzy_core.config_util.SyncedConfigHelperV1
import net.minecraft.util.Identifier
import java.util.function.Predicate

open class ValidatedIntList(
    defaultValue:List<Identifier>,
    listEntryValidator: Predicate<Identifier> = Predicate {true},
    invalidEntryMessage: String = "None",
    :
    ValidatedList<Identifier>(
        defaultValue,
        Identifier::class.java,
        listEntryValidator,
        invalidEntryMessage
    ) 
{
}
