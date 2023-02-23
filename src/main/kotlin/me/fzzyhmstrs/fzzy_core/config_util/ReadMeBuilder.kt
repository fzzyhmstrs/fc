package me.fzzyhmstrs.fzzy_core.config_util

import me.fzzyhmstrs.fzzy_core.FC
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

open class ReadMeBuilder(private val file: String, private val base: String = FC.MOD_ID, headerText: List<String> = listOf()):
    ReadMeWriter {

    private val readMeList: MutableList<String> = headerText.toMutableList()

    override fun readmeText(): List<String>{
        return readMeList
    }

    open fun writeReadMe(){
        writeReadMe(file, base)
    }

    open fun build(): List<String>{
        for (it in this.javaClass.kotlin.declaredMemberProperties){
            if (it is KMutableProperty<*>){
                val propVal = it.get(this)
                if (propVal is ReadMeBuilder){
                    readMeList.addAll(propVal.build())
                } else if(propVal is ReadMeTextProvider) {
                    //prioritize an added annotation over the default/builtin readmeText()
                    val annotation = it.findAnnotation<ReadMeText>()
                    if (annotation != null){
                        readMeList.add(readMeLineDecorator(annotation.description,it.name))
                    } else {
                        readMeList.add(readMeLineDecorator(propVal.readmeText(), it.name))
                    }
                } else{
                    val annotation = it.findAnnotation<ReadMeText>()
                    if (annotation != null){
                        readMeList.add(readMeLineDecorator(annotation.description,it.name))
                    }
                }
            }
        }
        return readmeText()
    }

    open fun readMeLineDecorator(rawLine: String, propName: String): String{
        return " > $propName: $rawLine"
    }

    open fun addToReadMe(list: List<String>){
        readMeList.addAll(list)
    }
}
