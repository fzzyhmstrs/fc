package me.fzzyhmstrs.fzzy_core.config_util

import net.minecraft.network.PacketByteBuf
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaType

interface ClientServerSynced{
    fun readFromServer(buf: PacketByteBuf){
        val nameMap: MutableMap<String,KProperty1<*,*>> = mutableMapOf()
        for (it in this.javaClass.kotlin.declaredMemberProperties){
            nameMap[it.name] = it
        }
        val propCount = buf.readInt()
        for (i in 1..propCount{
            val name = buf.readString()
            val prop = nameMap[name]?:throw IllegalStateException("PacketByteBuf reader had a problem resolving member name $name in the deserializing class ${this.javaClass.simpleName}")
            val propVal = prop.get(this)
            if (propVal is ClientServerSynced){
                propVal.readFromServer(buf)
            } else if(it is KMutableProperty<*>){
                propVal.setter.call(this,propertyFromBuf(propVal,propVal.get(this),buf))
            }
        }
    }
    
    fun writeToClient(buf: PacketByteBuf){
        buf.writeInt(this.javaClass.kotlin.declaredMemberProperties.size)
        for (it in this.javaClass.kotlin.declaredMemberProperties){
            val propVal = it.get(this)
            if (propVal is ClientServerSynced){
                buf.writeString(it.name)
                propVal.writeToClient(buf)
            } else if (it is KMutableProperty<*>){
                buf.writeString(it.name)
                propertyToBuf(propVal,buf)
            }
        }
    }
    
    companion object{
        private val toBufMap: Map<Class<T>,ToBufWriter<T>> = mapOf(
            Int.javaClass to ToBufWriter<Int> {p, buf -> buf.writeInt(p)},
            Float.javaClass to ToBufWriter<Float> {p, buf -> buf.writeFloat(p)},
            Double.javaClass to ToBufWriter<Double> {p, buf -> buf.writeDouble(p)},
            Long.javaClass to ToBufWriter<Long> {p, buf -> buf.writeLong(p)},
            Short.javaClass to ToBufWriter<Short> {p, buf -> buf.writeShort(p)},
            Byte.javaClass to ToBufWriter<Byte> {p, buf -> buf.writeByte(p)},
            String.javaClass to ToBufWriter<String> {p, buf -> buf.writeString(p)},
            Boolean.javaClass to ToBufWriter<Boolean> {p, buf -> buf.writeBoolean(p)},
            Identifier.javaClass to ToBufWriter<Identifier> {p, buf -> buf.writeIdentifier(p)}
        )
        
         private val fromBufMap: Map<Class<T>,FromBufReader<T>> = mapOf(
            Int.javaClass to FromBufReader<Int> {buf -> buf.readInt()},
            Float.javaClass to FromBufReader<Float> {buf -> buf.readFloat()},
            Double.javaClass to FromBufReader<Double> {buf -> buf.readDouble()},
            Long.javaClass to FromBufReader<Long> {buf -> buf.readLong()},
            Short.javaClass to FromBufReader<Short> {buf -> buf.readShort()},
            Byte.javaClass to FromBufReader<Byte> {buf -> buf.readByte()},
            String.javaClass to FromBufReader<String> {buf -> buf.readString()},
            Boolean.javaClass to FromBufReader<Boolean> {buf -> buf.readBoolean()},
            Identifier.javaClass to FromBufReader<Identifier> {buf -> buf.readIdentifier()}
        )
        
        private fun interface ToBufWriter<T>{
            fun toBuf(p: T, buf: PacketByteBuf)
        }
        
        private fun interface FromBufReader<T>{
            fun fromBuf(buf: PacketByteBuf): T
        }
        
        private fun propertyToBuf(prop: Any,buf: PacketByteBuf){
            toBufMap[prop.javaClass]?.toBuf(prop,buf)
        }
        
        private fun propertyFromBuf<T>(type: Class<T>,fallback: T, buf:PacketByteBuf): T{
            return fromBufMap[type].fromBuf(buf)?:fallback
        }
    }
}
