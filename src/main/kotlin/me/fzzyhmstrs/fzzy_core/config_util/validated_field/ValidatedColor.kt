package me.fzzyhmstrs.fzzy_core.config_util.validated_field

import me.fzzyhmstrs.fzzy_core.config_util.ConfigSection
import me.fzzyhmstrs.fzzy_core.config_util.SyncedConfigHelperV1
import java.util.function.Predicate

open class ValidatedColor(
    defaultR: Int,
    defaultG: Int,
    defaultB: Int,
    defaultA: Int = Int.MIN_VALUE
    headerText: List<String> = listOf(),
    decorator: LineDecorator = LineDecorator.DEFAULT)
    :
    ConfigSection(headerText,decorator)
{
    
    init{
        if(defaultR<0 || defaultR>255) throw IllegalArgumentException("Red portion of validated color not provided a default value between 0 and 255")
        if(defaultG<0 || defaultG>255) throw IllegalArgumentException("Green portion of validated color not provided a default value between 0 and 255")
        if(defaultB<0 || defaultB>255) throw IllegalArgumentException("Blue portion of validated color not provided a default value between 0 and 255")
        if((defaultA<0 && defaultA!=Int.MIN_VALUE) || defaultA>255) throw IllegalArgumentException("Transparency portion of validated color not provided a default value between 0 and 255")
    }
    
    var r = ValidatedInt(defaultR,0,255)
    var g = ValidatedInt(defaultG,0,255)
    var b = ValidatedInt(defaultB,0,255)
    var a = if(defaultA != Int.MIN_VALUE){ 
        ValidatedInt(defaultA,0,255)
    } else {
        ValidatedInt(255,255,255)
    }
    
    fun getAsInt():Int{
        return (r.get() shl 16) + (g.get() shl 8) + b.get()
    }
    
    fun getAsColor(): Color{
        return Color(r.get(),g.get(),b.get(),a.get())
    }
}
