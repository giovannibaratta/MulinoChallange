package it.unibo.mulino.qlearning.player

import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

fun main(args: Array<String>) {


    val semaphore = Semaphore(0)

    val enemyCmd = arrayOf(
            "java -jar barba.jar",
            "java -jar botquix.jar",
            "java -jar cook.jar",
            "java -jar deep.jar",
            "java -jar ginew.jar",
            //"java -jar gallina.jar",
            "java -jar unknown.jar",

            "java -jar barba.jar",
            "java -jar botquix.jar",
            "java -jar cook.jar",
            "java -jar deep.jar",
            "java -jar ginew.jar",
            //"java -jar gallina.jar",
            "java -jar unknown.jar"
    )

    val enemyWhite = arrayOf(
            "white -t 55",
            "White",
            "-w -t 55",
            "-w -t 55", // deep
            "-t 55",
            //"White",
            "White",


            "white -t 55",
            "White",
            "-w -t 55",
            "-w -t 55", // deep
            "-t 55",
            //"White",
            "White"
    )

    val enemyBlack = arrayOf(
            "black -t 55",
            "Black",
            "-b -t 55",
            "-b -t 55", // deep
            "-b -t 55",
            //"Black",
            "Black",


            "black -t 55",
            "Black",
            "-b -t 55",
            "-b -t 55", // deep
            "-b -t 55",
            //"Black",
            "Black"

    )

    val myClientW = "java -jar client.jar White qLearningApprendimento"
    val myClientB = "java -jar client.jar Black qLearningApprendimento"

    for (i in 0..5) {

        val player = ((Math.random() * 11) % 10).toInt()
        val white = (Math.random()) * 100 % 100

        println("\n\n!! Avversario $player , white $white !! \n\n")

        var pServer: Process? = null
        var pWhite: Process? = null
        var pBlack: Process? = null

        val killAll = {
            Thread.sleep(1000)
            val pS = pServer
            val pW = pWhite
            val pB = pBlack

            if (pS != null && pS.isAlive) {
                pS.destroyForcibly()
            }
            if (pW != null && pW.isAlive) {
                pW.destroyForcibly()
            }
            if (pB != null && pB.isAlive) {
                pB.destroyForcibly()
            }
        }

        thread {
            pServer = Runtime.getRuntime().exec("java -jar server.jar")
            val pLocal = pServer ?: throw IllegalStateException("Non sono riuscito ad avviare il processo")
            val buf = pLocal.inputStream.bufferedReader()
            val err = pLocal.errorStream.bufferedReader()
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
            val res = pLocal.waitFor()
            err.close()
            buf.close()
            println("Fine server $res")
            killAll()
            semaphore.release()
        }

        thread {
            Thread.sleep(300)
            pWhite = when (white > 50) {
                true -> Runtime.getRuntime().exec(myClientW)
                false -> Runtime.getRuntime().exec(enemyCmd[player] + " " + enemyWhite[player])
            }

            val pLocal = pWhite ?: throw IllegalStateException("Non sono riuscito ad avviare il processo")
            val buf = pLocal.inputStream.bufferedReader()
            val err = pLocal.errorStream.bufferedReader()
            val print = white > 50
            thread {
                var line = buf.readLine()
                while (line != null) {
                    if (print)
                        println(line)
                    line = buf.readLine()
                }
            }
            thread {
                var line = err.readLine()
                while (line != null) {
                    if (print)
                        println(line)
                    line = err.readLine()
                }
            }
            val res = pLocal.waitFor()
            err.close()
            buf.close()
            println("Fine p white $res")
            killAll()
            semaphore.release()
        }

        thread {
            Thread.sleep(400)
            pBlack = when (white > 50) {
                true -> Runtime.getRuntime().exec(enemyCmd[player] + " " + enemyBlack[player])
                false -> Runtime.getRuntime().exec(myClientB)
            }
            val pLocal = pBlack ?: throw IllegalStateException("Non sono riuscito ad avviare il processo")

            val buf = pLocal.inputStream.bufferedReader()
            val err = pLocal.errorStream.bufferedReader()
            val print = white <= 50
            thread {
                var line = buf.readLine()
                while (line != null) {
                    if (print)
                        println(line)
                    line = buf.readLine()
                }
            }
            thread {
                var line = err.readLine()
                while (line != null) {
                    if (print)
                        println(line)
                    line = err.readLine()
                }
            }
            pLocal.waitFor()
            err.close()
            buf.close()
            println("Fine p black mill")
            killAll()
            semaphore.release()
        }

        semaphore.acquire()
        semaphore.acquire()
        semaphore.acquire()
        println("\nFine step $i\n")
    }

    println("Fine training")
}