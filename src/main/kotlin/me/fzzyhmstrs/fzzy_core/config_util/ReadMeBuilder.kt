package me.fzzyhmstrs.fzzy_core.config_util

import me.fzzyhmstrs.fzzy_core.FC
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

open class ReadMeBuilder(
    private val file: String,
    private val base: String = FC.MOD_ID,
    headerText: List<String> = listOf(),
    private val decorator: LineDecorating,
    private val indentIncrement: Int = 0)
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

    open fun build(indent: Int = 0): List<String>{
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
                        readMeList.add(readMeLineDecorator(desc, it.name, indent))
                        continue
                    }
                }
                if (propVal is ReadMeBuilder){
                    readMeList.addAll(propVal.build(indent + indentIncrement))
                } else if(propVal is ReadMeTextProvider) {
                    //prioritize an added annotation over the default/builtin readmeText()
                    readMeList.add(readMeLineDecorator(propVal.readmeText(), it.name, indent))
                }
            }
        }
        return readmeText()
    }

    open fun readMeLineDecorator(rawLine: String, propName: String, indent: Int): String{
        return decorator.decorate(rawLine, propName, indent)
    }

    open fun addToReadMe(list: List<String>){
        readMeList.addAll(list)
    }

    enum class LineDecorator: LineDecorating{
        DEFAULT{
            override fun decorate(rawLine: String, propName: String, indent: Int): String {
                return "    ".repeat(indent) + " >> $propName: $rawLine"
            }
        },
        STAR{
            override fun decorate(rawLine: String, propName: String, indent: Int): String {
                return "    ".repeat(indent) + " * $propName: $rawLine"
            }
        },
        DOUBLE_SPACED{
            override fun decorate(rawLine: String, propName: String, indent: Int): String {
                return "    ".repeat(indent) + " >> $propName: $rawLine\n"
            }
        },
        BRACKET{
            override fun decorate(rawLine: String, propName: String, indent: Int): String {
                return "....".repeat(indent) + "[$propName]: $rawLine"
            }
        }
    }

    fun interface LineDecorating{
        fun decorate(rawLine: String, propName: String, indent: Int): String
    }

    class HeaderBuilder(){
        val list: MutableList<String> = mutableListOf()

        fun build(): List<String>{
            return list
        }

        fun space(): HeaderBuilder{
            list.add("")
            return this
        }

        fun add(line: String): HeaderBuilder{
            list.add(line)
            return this
        }

        fun underscore(line: String): HeaderBuilder{
            list.add(line)
            list.add("-".repeat(line.length))
            return this
        }

        fun overscore(line: String): HeaderBuilder{
            list.add("_".repeat(line.length))
            list.add(line)
            return this
        }

        fun underoverscore(line: String): HeaderBuilder{
            list.add("-".repeat(line.length))
            list.add(line)
            list.add("-".repeat(line.length))
            return this
        }

        fun box(line: String): HeaderBuilder{
            list.add("#".repeat(line.length+4))
            list.add("# $line #")
            list.add("#".repeat(line.length+4))
            return this
        }

    }

}
