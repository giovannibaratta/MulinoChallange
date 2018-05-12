package it.unibo

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
            "java -jar gallina.jar",
            "java -jar unknown.jar"
    )

    val enemyWhite = arrayOf(
            "white -t 55",
            "White",
            "-w -t 55",
            "-w -t 55", // deep
            "-t 55",
            "White",
            "White"
    )

    val enemyBlack = arrayOf(
            "black -t 55",
            "Black",
            "-b -t 55",
            "-b -t 55", // deep
            "-b -t 55",
            "Black",
            "Black"
    )

    val myClientW = "java -jar client.jar White qLearning"
    val myClientB = "java -jar client.jar Black qLearning"
    var sconfitte = 0
    var vittorie = 0
    var pServer: Process? = null
    var pWhite: Process? = null
    var pBlack: Process? = null
    val killAll = { pS: Process?, pW: Process?, pB: Process? ->
        Thread.sleep(1000)

        if (pS != null && pS.isAlive) pS.destroyForcibly()
        if (pW != null && pW.isAlive) pW.destroyForcibly()
        if (pB != null && pB.isAlive) pB.destroyForcibly()
    }

    for (i in enemyCmd.indices) {

        println("\n\n!! Avversario $player\n\n")

        thread {
            pServer = Runtime.getRuntime().exec("java -jar server.jar")
            val pLocal = pServer ?: throw IllegalStateException("Non sono riuscito ad avviare il processo")
            val buf = pLocal.inputStream.bufferedReader()
            val err = pLocal.errorStream.bufferedReader()
            thread {
                var line = buf.readLine()
                while (line != null) {
                    println(line)
                    if (line.compareTo("Player B WIN!!!") == 0) {
                        sconfitte++
                    } else if (line.compareTo("Player W WIN!!!") == 0) {
                        vittorie++
                    }
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
            killAll(pServer, pWhite, pBlack)
            semaphore.release()
        }

        thread {
            Thread.sleep(300)
            pWhite = Runtime.getRuntime().exec(myClientW)

            val pLocal = pWhite ?: throw IllegalStateException("Non sono riuscito ad avviare il processo")
            val buf = pLocal.inputStream.bufferedReader()
            val err = pLocal.errorStream.bufferedReader()
            thread {
                var line = buf.readLine()
                while (line != null) {
                    //println(line)
                    line = buf.readLine()
                }
            }
            thread {
                var line = err.readLine()
                while (line != null) {
                    //println(line)
                    line = err.readLine()
                }
            }
            val res = pLocal.waitFor()
            err.close()
            buf.close()
            println("Fine p white $res")
            killAll(pServer, pWhite, pBlack)
            semaphore.release()
        }

        thread {
            Thread.sleep(400)
            pBlack = Runtime.getRuntime().exec(enemyCmd[i] + " " + enemyBlack[i])
            val pLocal = pBlack ?: throw IllegalStateException("Non sono riuscito ad avviare il processo")

            val buf = pLocal.inputStream.bufferedReader()
            val err = pLocal.errorStream.bufferedReader()
            thread {
                var line = buf.readLine()
                while (line != null) {
                    //println(line)
                    line = buf.readLine()
                }
            }
            thread {
                var line = err.readLine()
                while (line != null) {
                    //println(line)
                    line = err.readLine()
                }
            }
            pLocal.waitFor()
            err.close()
            buf.close()
            println("Fine p black mill")
            killAll(pServer, pWhite, pBlack)
            semaphore.release()
        }

        semaphore.acquire()
        semaphore.acquire()
        semaphore.acquire()
        println("\nFine parita $i\n")


        println("\n\n\n STAT PARZIALE $vittorie vittore su ${vittorie + sconfitte}\n\n\n")
    }

    for (i in enemyCmd.indices) {

        println("\n\n!! Avversario $player\n\n")

        thread {
            pServer = Runtime.getRuntime().exec("java -jar server.jar")
            val pLocal = pServer ?: throw IllegalStateException("Non sono riuscito ad avviare il processo")
            val buf = pLocal.inputStream.bufferedReader()
            val err = pLocal.errorStream.bufferedReader()
            thread {
                var line = buf.readLine()
                while (line != null) {
                    println(line)
                    if (line.compareTo("Player W WIN!!!") == 0) {
                        sconfitte++
                    } else if (line.compareTo("Player B WIN!!!") == 0) {
                        vittorie++
                    }
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
            killAll(pServer, pWhite, pBlack)
            semaphore.release()
        }

        thread {
            Thread.sleep(300)
            pWhite = Runtime.getRuntime().exec(enemyCmd[i] + " " + enemyWhite[i])
            val pLocal = pWhite ?: throw IllegalStateException("Non sono riuscito ad avviare il processo")

            val buf = pLocal.inputStream.bufferedReader()
            val err = pLocal.errorStream.bufferedReader()
            thread {
                var line = buf.readLine()
                while (line != null) {
                    //println(line)
                    line = buf.readLine()
                }
            }
            thread {
                var line = err.readLine()
                while (line != null) {
                    //println(line)
                    line = err.readLine()
                }
            }
            pLocal.waitFor()
            err.close()
            buf.close()
            killAll(pServer, pWhite, pBlack)
            semaphore.release()
        }

        thread {
            Thread.sleep(400)
            pBlack = Runtime.getRuntime().exec(myClientB)

            val pLocal = pBlack ?: throw IllegalStateException("Non sono riuscito ad avviare il processo")
            val buf = pLocal.inputStream.bufferedReader()
            val err = pLocal.errorStream.bufferedReader()
            thread {
                var line = buf.readLine()
                while (line != null) {
                    //println(line)
                    line = buf.readLine()
                }
            }
            thread {
                var line = err.readLine()
                while (line != null) {
                    //println(line)
                    line = err.readLine()
                }
            }
            val res = pLocal.waitFor()
            err.close()
            buf.close()
            killAll(pServer, pWhite, pBlack)
            semaphore.release()
        }

        semaphore.acquire()
        semaphore.acquire()
        semaphore.acquire()
        println("\nFine parita $i\n")


        println("\n\n\n STAT PARZIALE $vittorie vittore su ${vittorie + sconfitte}\n\n\n")
    }

    println("Fine competizione")
    println("$vittorie vittore su ${vittorie + sconfitte}")
}