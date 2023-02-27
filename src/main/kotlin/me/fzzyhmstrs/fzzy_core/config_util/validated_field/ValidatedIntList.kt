package me.fzzyhmstrs.fzzy_core.config_util.validated_field

import me.fzzyhmstrs.fzzy_core.config_util.SyncedConfigHelperV1
import java.util.function.BiPredicate

open class ValidatedIntList(
    defaultValue:List<Int>,
    listEntryValidator: Predicate<Int> = Predicate {true},
    invalidEntryMessage: String = "None",
    :
    ValidatedList<Int>(
        defaultValue,
        Int::class.java,
        listEntryValidator,
        invalidEntryMessage
    ) 
{
}
