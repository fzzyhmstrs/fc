package me.fzzyhmstrs.fzzy_core.nbt_util

/**
 * various Nbt keys used by Amethyst Imbuement and other Amethyst mods. Use at your own risk!
 */
enum class NbtKeys {

    ITEM_STACK_ID{
        override fun str(): String {
            return "item_stack_id"
        }
    },
    ACTIVE_ENCHANT{
        override fun str(): String {
            return "active_enchant_id"
        }
    },
    MOD_INIT{
        override fun str(): String {
            return "mod_init_"
        }
    },
    LAST_USED{
        override fun str(): String {
            return "_last_used"
        }
    },
    LAST_USED_LIST{
        override fun str(): String {
            return "last_used_list"
        }
    };

    abstract fun str(): String
}