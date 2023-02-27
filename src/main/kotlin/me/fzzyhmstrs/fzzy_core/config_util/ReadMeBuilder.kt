package me.fzzyhmstrs.fzzy_core.config_util

import me.fzzyhmstrs.fzzy_core.FC
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

open class ReadMeBuilder(
    private val file: String,
    private val base: String = FC.MOD_ID,
    headerText: List<String> = listOf(),
    private val decorator: Decorator)
    :
    ReadMeWriter
{

    private val readMeList: MutableList<String> = headerText.toMutableList()

    override fun readmeText(): List<String>{
        return readMeList
    }

    open fun writeReadMe(){
        writeReadMe(file, base)
    }

    open fun build(): List<String>{
        val fields = this::class.java.declaredFields
        val orderById = fields.withIndex().associate { it.value.name to it.index }
        for (it in this.javaClass.kotlin.declaredMemberProperties.sortedBy { orderById[it.name] }){
            if (it is KMutableProperty<*>) {
                val propVal = it.get(this)
                val annotation = it.findAnnotation<ReadMeText>()
                if(annotation != null){
                    val desc = annotation.description
                    val header = annotation.header
                    if(header.isNotEmpty()){
                        readMeList.addAll(header)
                    }
                    if (desc != "") {
                        readMeList.add(readMeLineDecorator(desc, it.name))
                        continue
                    }
                }
                if (propVal is ReadMeBuilder){
                    readMeList.addAll(propVal.build())
                } else if(propVal is ReadMeTextProvider) {
                    //prioritize an added annotation over the default/builtin readmeText()
                    readMeList.add(readMeLineDecorator(propVal.readmeText(), it.name))
                }
            }
        }
        return readmeText()
    }

    open fun readMeLineDecorator(rawLine: String, propName: String): String{
        return decorator.decorate(rawLine, propName)
    }

    open fun addToReadMe(list: List<String>){
        readMeList.addAll(list)
    }

    enum class Decorator{
        DEFAULT{
            override fun decorate(rawLine: String, propName: String): String {
                return " >> $propName: $rawLine"
            }
        },
        INNER{
            override fun decorate(rawLine: String, propName: String): String {
                return "     > $propName: $rawLine"
            }
        },
        STAR{
            override fun decorate(rawLine: String, propName: String): String {
                return " * $propName: $rawLine"
            }
        },
        STAR_INNER{
            override fun decorate(rawLine: String, propName: String): String {
                return "     * $propName: $rawLine"
            }
        },
        DOUBLE_SPACED{
            override fun decorate(rawLine: String, propName: String): String {
                return " >> $propName: $rawLine\n"
            }
        },
        DOUBLE_SPACED_INNER{
            override fun decorate(rawLine: String, propName: String): String {
                return "     > $propName: $rawLine\n"
            }
        },
        BRACKET{
            override fun decorate(rawLine: String, propName: String): String {
                return "[$propName]: $rawLine"
            }
        },
        BRACKET_INNER{
            override fun decorate(rawLine: String, propName: String): String {
                return "....[$propName]: $rawLine"
            }
        };

        abstract fun decorate(rawLine: String, propName: String): String
    }
}
