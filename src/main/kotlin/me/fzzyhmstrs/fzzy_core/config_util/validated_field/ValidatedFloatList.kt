package me.fzzyhmstrs.fzzy_core.config_util.validated_field

import me.fzzyhmstrs.fzzy_core.config_util.SyncedConfigHelperV1
import java.util.function.BiPredicate

open class ValidatedIntList(
    defaultValue:List<Float>,
    listEntryValidator: Predicate<Float> = Predicate {true},
    invalidEntryMessage: String = "None",
    :
    ValidatedList<Float>(
        defaultValue,
        Float::class.java,
        listEntryValidator,
        invalidEntryMessage
    ) 
{
}
