package com.feint

import java.io.File
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

import java.util.*
import kotlin.collections.HashMap

/**
 * 纪录所监听文件夹的所有文件信息（文件的名称，文件的MD5值）
 */
class FileHandle {
    val fileMap=HashMap<String,FileInfo>();
    val logList=LinkedList<FileInfo>()
    fun addFile(fileName: String,info:FileInfo) {
        fileMap.put(fileName,info);
        //只有文件状态发生改变时，进行日志记录
        if(info.state!=FileState.NO_CHANGE)
            logList.add(info)
    }

    /**
     * 根据目的文件中的文件信息，初始化fileMap。
     */
    fun initHandle(dstDir:String){
        val dir= File(dstDir)
        dir.list{ dir,name->
            fileMap.put(name,
                    FileInfo(name,MD5Util.md5(File("$dir/$name").inputStream()),FileState.NO_CHANGE))
            return@list true
        }
    }

    override fun toString(): String {
        var str:String="fileName\t|\tmd5Value\t\t\t\t\t|\tstate\n" +
                "=========================================================\n";
        fileMap.forEach { key,value->
            str+="${value.fileName}\t|\t${value.md5}\t|\t${value.state}\n"
        }
        return str
    }

    fun printLog():String{
        var str:String="fileName\t|\tmd5Value\t\t\t\t\t|\tstate\t\t|update time\n" +
                "=========================================================\n";
        logList.forEach { it->
            str+="${it.fileName}\t|\t${it.md5}\t|\t${it.state}\t\t|${it.time.toString()}\n"
        }
        return str
    }
}

class FileInfo(val fileName: String,val md5:String,val state:FileState,val time:Date= Date())

object MD5Util{

    fun md5(input:InputStream):String= BigInteger(1,
        MessageDigest.getInstance("MD5").digest(input.readBytes())).toString(16);
}

/**
 * 1. src中增添新的文件(src中文件名称和内容都发生改变)
 * 2. src中文件的名称发生改变，内容不变
 * 3. src中文件名称没变，内容发生改变
 * 4. src中删除了文件
 */
enum class FileState{
    NEW_FILE,
    DEL_FILE,
    CONT_CHANGE,
    NO_CHANGE
}
