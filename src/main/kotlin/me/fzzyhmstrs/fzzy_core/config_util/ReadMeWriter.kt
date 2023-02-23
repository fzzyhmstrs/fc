package me.fzzyhmstrs.fzzy_core.config_util

import me.fzzyhmstrs.fzzy_core.FC
import java.io.File
import java.io.FileWriter

interface ReadMeWriter{
    fun writeReadMe(file: String, base: String = FC.MOD_ID){
        val textLines: List<String> = readmeText()
        val dirPair = SyncedConfigHelperV1.makeDir("", base)
        if (!dirPair.second){
            println("Couldn't make directory for storing the readme")
        }
        val f = File(dirPair.first,file)
        val fw = FileWriter(f)
        textLines.forEach {
                value -> fw.write(value)
            fw.write(System.getProperty("line.separator"))
        }
        fw.close()
    }

    fun readmeText(): List<String>
}