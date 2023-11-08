package me.fzzyhmstrs.fzzy_core.modifier_util

import java.util.*

open class SlotId(private val id: String) {

    fun getUUID(prefix: String): UUID{
        return UUID.nameUUIDFromBytes((prefix + id).getBytes())
    }

    override hashcode(): Int{
        return id.hashcode()
    }

    override equals(other: Any?): Boolean{
        return (other as? SlotId)?.id == this.id
    }

    companion object{

        val HEAD = SlotId(EquipmentSlot.HEAD.getName())
        val CHEST = SlotId(EquipmentSlot.CHEST.getName())
        val LEGS = SlotId(EquipmentSlot.LEGS.getName())
        val FEET = SlotId(EquipmentSlot.FEET.getName())
        val MAINHAND = SlotId(EquipmentSlot.MAINHAND.getName())
        val OFFHAND = SlotId(EquipmentSlot.CHEST.getName())

        private val slotIds: Array<SlotId> = listOf(HEAD, CHEST, LEGS, FEET, MAINHAND, OFFHAND).toTypedArray()

        fun getIdBySlot(slot: EquipmentSlot): SlotId{
            return slotIds[slot.ordinal]
        }
        
    }
    
}
