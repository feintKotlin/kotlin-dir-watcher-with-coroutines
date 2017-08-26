package com.feint

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.util.*

fun main(args: Array<String>) {

    val fileHandle=FileHandle()
    val scanner:Scanner= Scanner(System.`in`)
    val srcPath="/Users/feint/Desktop/filesyn";
    val dstPath="/Users/feint/Desktop/filesyn_cp"
    val file:File=File(srcPath);

    fileHandle.initHandle(dstPath)

    val job=launch(CommonPool) {
        while (true) {

            //获取目标文件夹的文件名列表
            val dstFilelist = File("$dstPath").list().toMutableList();
            //遍历原文件夹
            file.list { dir, name ->

                val sfile = File("$dir/$name");
                val dfile:File=File("$dstPath/$name");
                //获取原文件夹中的这个文件的MD5，用于判断文件内容是否发生改变
                val sMd5 = MD5Util.md5(sfile.inputStream())

                //根据文件名，判断当前文件是否已经存在
                if (fileHandle.fileMap[name]!=null) {

                    val dMd5 = MD5Util.md5(dfile.inputStream())
                    //比较原文件和目的文件的MD5值，判断文件内容是否发生改变
                    if (sMd5 == dMd5) {
                        //当两文件的MD5值相同时执行以下操作
                        fileHandle.addFile(name,FileInfo(name, dMd5, FileState.NO_CHANGE))
                        //将该文件名从目标文件夹的文件名列表中移除
                        dstFilelist.remove(name);
                        return@list true
                    }
                    //当两个文件的MD5不相同时，表示文件内容发生改变，将目的文件删除
                    dfile.delete()
                    fileHandle.addFile(name,FileInfo(name, sMd5, FileState.CONT_CHANGE))

                } else
                    fileHandle.addFile(name,FileInfo(name, sMd5, FileState.NEW_FILE))
                //将原文件的内容复制到目的文件中
                sfile.copyTo(dfile)
                //将该文件名从目标文件夹的文件名列表中移除
                dstFilelist.remove(name);
                return@list true;
            }
            //此时目标文件夹的文件名列表中剩下的项都是，已经在原文件夹中不存在的文件；将其删除
            dstFilelist.forEach {
                val delFile = File("$dstPath/$it")
                fileHandle.addFile(it,FileInfo(it, MD5Util.md5(delFile.inputStream()), FileState.DEL_FILE))
                delFile.delete();
            }
            //每隔2.5秒更新一次信息
            delay(2500)

        }
    }
    out@while (true){
        println("1: 查看当前文件夹信息\t\t2: 查看日志信息\t\t3: 关闭程序")
        try {
            val command=scanner.next().toInt()
            when(command){
                1-> println(fileHandle.toString())
                2-> println(fileHandle.printLog())
                3->{
                    job.cancel()
                    return@out
                }
                else-> println("对不起，您输入的命令不存在！")
            }
        }catch (e:Exception){
            println("输入信息有误，请重新输入")
        }
    }

    println("程序结束！！")
}