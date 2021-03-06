package it.unibo

import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

/**
 * Main per simulare un torneo contro gli altri giocatori.
 * TODO("Le raccolta delle statistiche è approssimativa e quindi i risultati sono sbagliati")
 */
fun main(args: Array<String>) {


    val semaphore = Semaphore(0)

    val enemyCmd = arrayOf(
            "java -jar barba.jar",
            "java -jar botquix.jar",
            "java -jar cook.jar",
            "java -jar deep.jar",
            "java -jar gallina.jar",
            "java -jar ginew.jar",
            "java -jar unknown.jar"
    )

    val enemyWhite = arrayOf(
            "white -t 15",
            "White",
            "-w -t 15",
            "-w -t 15", // deep
            "-t 15",
            "White",
            "White"
    )

    val enemyBlack = arrayOf(
            "black -t 15",
            "Black",
            "-b -t 15",
            "-b -t 15", // deep
            "-b -t 15",
            "Black",
            "Black"
    )

    val myClientW = "java -jar client.jar White TrainerMinMax"
    val myClientB = "java -jar client.jar Black TrainerMinMax"
    var sconfitte = 0
    var partite = 0
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

        println("\n\n!! Avversario ${enemyCmd[i]}\n\n")

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
                partite++
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


        println("\n\n\n STAT PARZIALE $vittorie vittore su ${partite}\n\t$sconfitte sconfitte su ${partite}\n\n")
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


        println("\n\n\n STAT PARZIALE $vittorie vittore su ${partite}\n\t$sconfitte sconfitte su ${partite}\n\n")
    }

    println("Fine competizione")
    println("$vittorie vittore su ${partite}\n\t$sconfitte sconfitte su ${partite}\n\n")
}