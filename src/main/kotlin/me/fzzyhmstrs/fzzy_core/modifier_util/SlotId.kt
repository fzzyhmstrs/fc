package me.fzzyhmstrs.fzzy_core.modifier_util

import net.minecraft.entity.EquipmentSlot
import java.util.*

open class SlotId(private val id: String) {

    fun getUUID(prefix: String): UUID{
        return UUID.nameUUIDFromBytes("$prefix@$id".toByteArray())
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean{
        if (other !is SlotId) return false
        return other.id == this.id
    }

    companion object{

        val HEAD = SlotId(EquipmentSlot.HEAD.getName())
        val CHEST = SlotId(EquipmentSlot.CHEST.getName())
        val LEGS = SlotId(EquipmentSlot.LEGS.getName())
        val FEET = SlotId(EquipmentSlot.FEET.getName())
        val MAINHAND = SlotId(EquipmentSlot.MAINHAND.getName())
        val OFFHAND = SlotId(EquipmentSlot.CHEST.getName())

        private val slotIds: Array<SlotId>

        init{
            val list: MutableList<SlotId> = mutableListOf()
            list.add(MAINHAND)
            list.add(OFFHAND)
            list.add(FEET)
            list.add(LEGS)
            list.add(CHEST)
            list.add(HEAD)
            slotIds = list.toTypedArray()
        }

        fun getIdBySlot(slot: EquipmentSlot): SlotId{
            return slotIds[slot.ordinal]
        }
        
    }
    
}
