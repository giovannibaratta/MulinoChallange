package it.unibo.mulino.qlearning.player

import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

fun main(args: Array<String>) {


    val semaphore = Semaphore(0)

    for (i in 0..50) {


        thread {
            val p = Runtime.getRuntime().exec("java -jar server.jar")
            val buf = p.inputStream.bufferedReader()
            val err = p.errorStream.bufferedReader()
            thread {
                var line = buf.readLine()
                while (line != null) {
                    println(line)
                    line = buf.readLine()
                }
            }
            thread {
                var line = err.readLine()
                while (line != null) {
                    println(line)
                    line = err.readLine()
                }
            }
            val res = p.waitFor()
            err.close()
            buf.close()
            println("Fine server $res")
            semaphore.release()
        }

        thread {
            Thread.sleep(300)
            val p = Runtime.getRuntime().exec("java -jar client.jar White qLearning")
            val buf = p.inputStream.bufferedReader()
            val err = p.errorStream.bufferedReader()
            thread {
                var line = buf.readLine()
                while (line != null) {
                    println(line)
                    line = buf.readLine()
                }
            }
            thread {
                var line = err.readLine()
                while (line != null) {
                    println(line)
                    line = err.readLine()
                }
            }
            val res = p.waitFor()
            err.close()
            buf.close()
            println("Fine qlearning $res")
            semaphore.release()
        }

        thread {
            Thread.sleep(400)
            val p = Runtime.getRuntime().exec("java -jar deep.jar -b -t 115")
            val buf = p.inputStream.bufferedReader()
            val err = p.errorStream.bufferedReader()
            thread {
                var line = buf.readLine()
                while (line != null) {
                    println(line)
                    line = buf.readLine()
                }
            }
            thread {
                var line = err.readLine()
                while (line != null) {
                    println(line)
                    line = err.readLine()
                }
            }
            val res = p.waitFor()
            err.close()
            buf.close()
            println("Fine deep mill")
            semaphore.release()
        }

        semaphore.acquire()
        semaphore.acquire()
        semaphore.acquire()
        println("\nFine step $i\n")
    }

    println("Fine training")

    /*for(i in 0 .. 10){

    }*/


}