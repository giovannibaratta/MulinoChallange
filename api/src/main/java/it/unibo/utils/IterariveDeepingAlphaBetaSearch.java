package it.unibo.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;


/**
 * Implements an iterative deepening Minimax search with alpha-beta pruning and
 * action ordering. Maximal computation time is specified in seconds. The
 * algorithm is implemented as template method and can be configured and tuned
 * by subclassing.
 *
 * @param <S> Type which is used for states in the game.
 * @param <A> Type which is used for actions in the game.
 * @param <P> Type which is used for players in the game.
 * @author Ruediger Lunde
 */
public class IterariveDeepingAlphaBetaSearch<S, A, P> implements AdversarialSearch<S, A> {

    public final static String METRICS_NODES_EXPANDED = "nodesExpanded";
    public final static String METRICS_MAX_DEPTH = "maxDepth";
    public final static String METRICS_PRUNE = "prune";

    protected Game<S, A, P> game;
    protected double utilMax;
    protected double utilMin;
    protected int currDepthLimit;
    private boolean heuristicEvaluationUsed; // indicates that non-terminal
    public boolean limit = true;
    // nodes
    // have been evaluated.
    private Timer timer;
    private boolean logEnabled;

    private Metrics metrics = new Metrics();

    /**
     * Creates a new search object for a given game.
     *
     * @param game    The game.
     * @param utilMin Utility value of worst state for this player. Supports
     *                evaluation of non-terminal states and early termination in
     *                situations with a safe winner.
     * @param utilMax Utility value of best state for this player. Supports
     *                evaluation of non-terminal states and early termination in
     *                situations with a safe winner.
     * @param time    Maximal computation time in seconds.
     */
    public static <STATE, ACTION, PLAYER> IterariveDeepingAlphaBetaSearch<STATE, ACTION, PLAYER> createFor(
            Game<STATE, ACTION, PLAYER> game, double utilMin, double utilMax, int time) {
        return new IterariveDeepingAlphaBetaSearch<>(game, utilMin, utilMax, time);
    }

    /**
     * Creates a new search object for a given game.
     *
     * @param game    The game.
     * @param utilMin Utility value of worst state for this player. Supports
     *                evaluation of non-terminal states and early termination in
     *                situations with a safe winner.
     * @param utilMax Utility value of best state for this player. Supports
     *                evaluation of non-terminal states and early termination in
     *                situations with a safe winner.
     * @param time    Maximal computation time in seconds.
     */
    public IterariveDeepingAlphaBetaSearch(Game<S, A, P> game, double utilMin, double utilMax,
                                           int time) {
        this.game = game;
        this.utilMin = utilMin;
        this.utilMax = utilMax;
        this.timer = new Timer(time);
    }

    public void setLogEnabled(boolean b) {
        logEnabled = b;
    }

    /**
     * Template method controlling the search. It is based on iterative
     * deepening and tries to make to a good decision in limited time. Credit
     * goes to Behi Monsio who had the idea of ordering actions by utility in
     * subsequent depth-limited search runs.
     */
    @Override
    public A makeDecision(S state) throws ExecutionException {
        metrics = new Metrics();
        metrics.set(METRICS_PRUNE, 0);
        StringBuffer logText = null;
        P player = game.getPlayer(state);
        List<A> results = orderActions(state, game.getActions(state), player, 0);
        setLogEnabled(true);
        timer.start();
        /*
        if(!limit)
            currDepthLimit = 5;
        else
            currDepthLimit = 0;*/

        // creo il thread pool
        ArrayBlockingQueue<ParallelComputation> threadPool = new ArrayBlockingQueue<>(2);

        try {
            ParallelComputation thread = new ParallelComputation();
            threadPool.put(thread);
            new Thread(thread).start();
            thread = new ParallelComputation();
            threadPool.put(thread);
            new Thread(thread).start();
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new ExecutionException("non sono riuscito ad creare i thread", e);
        }

        Semaphore wait = new Semaphore(threadPool.size());
        List<ParallelComputation> workingThread = Collections.synchronizedList(new ArrayList<>());
        Object mutex = new Object();

        currDepthLimit = 0;
        do {
            incrementDepthLimit();
            if (logEnabled)
                logText = new StringBuffer("\n[Depth " + currDepthLimit + "]\n");
            heuristicEvaluationUsed = false;
            ActionStore<A> newResults = new ActionStore<>();



            /* OLD

            for (A action : results) {
                double value = minValue(game.getResult(state, action), player, Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY, 1);
                if (timer.timeOutOccurred())
                    break; // exit from action loop
                newResults.add(action, value);
                if (logEnabled)
                    logText.append(action).append("->").append(value).append(" \n");
            }*/
            Object mutex2 = new Object();
            //synchronized (mutex) {
            for (int i = 0; i < results.size(); i++) {
                final A action = results.get(i);
                try {
                    wait.acquire();
                } catch (InterruptedException e) {
                    throw new ExecutionException("Il main è stato interrotto", e);
                }
                ParallelComputation thread = threadPool.poll();
                thread.startSearch(new SearchPar(state, action, player), () -> {
                    //synchronized (mutex2) {
                    workingThread.remove(thread);
                    try {
                        threadPool.put(thread);
                    } catch (InterruptedException e) {
                        return;
                    }
                    newResults.add(action, thread.getResult());
                    wait.release();
                    //}
                });
                workingThread.add(thread);
                if (timer.timeOutOccurred())
                    break; // exit from action loop
            }
            //}

            if (logEnabled)
                System.out.println(logText);
            if (newResults.size() > 0) {
                results = newResults.actions;
                if (!timer.timeOutOccurred()) {
                    if (hasSafeWinner(newResults.utilValues.get(0)))
                        break; // exit from iterative deepening loop
                    else if (newResults.size() > 1
                            && isSignificantlyBetter(newResults.utilValues.get(0), newResults.utilValues.get(1)))
                        break; // exit from iterative deepening loop
                }
            }
            //System.out.println("Depth "+getMetrics().getInt(METRICS_MAX_DEPTH)+" complete. Nodes expanded "+getMetrics().getInt(METRICS_NODES_EXPANDED)+ " Best action :"+newResults.actions.get(0)+" "+newResults.utilValues.get(0)+".");
        } while (!timer.timeOutOccurred() && heuristicEvaluationUsed);

        threadPool.forEach((ParallelComputation p) -> p.stopWork());
        workingThread.forEach((ParallelComputation p) -> p.stopWork());

        return results.get(0);
    }

    // returns an utility value
    public double maxValue(S state, P player, double alpha, double beta, int depth) {
        updateMetrics(depth);
        if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccurred()) {
            return eval(state, player);
        } else {
            double value = Double.NEGATIVE_INFINITY;
            for (A action : orderActions(state, game.getActions(state), player, depth)) {
                value = Math.max(value, minValue(game.getResult(state, action), //
                        player, alpha, beta, depth + 1));
                if (value >= beta) {
                    metrics.incrementInt(METRICS_PRUNE);
                    return value;
                }
                alpha = Math.max(alpha, value);
            }
            return value;
        }
    }

    // returns an utility value
    public double minValue(S state, P player, double alpha, double beta, int depth) {
        updateMetrics(depth);
        if (game.isTerminal(state) || depth >= currDepthLimit || timer.timeOutOccurred()) {
            return eval(state, player);
        } else {
            double value = Double.POSITIVE_INFINITY;
            for (A action : orderActions(state, game.getActions(state), player, depth)) {
                value = Math.min(value, maxValue(game.getResult(state, action), //
                        player, alpha, beta, depth + 1));
                if (value <= alpha) {
                    metrics.incrementInt(METRICS_PRUNE);
                    return value;
                }
                beta = Math.min(beta, value);
            }
            return value;
        }
    }

    private void updateMetrics(int depth) {
        metrics.incrementInt(METRICS_NODES_EXPANDED);
        metrics.set(METRICS_MAX_DEPTH, Math.max(metrics.getInt(METRICS_MAX_DEPTH), depth));
    }

    /**
     * Returns some statistic data from the last search.
     */
    @Override
    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Primitive operation which is called at the beginning of one depth limited
     * search step. This implementation increments the current depth limit by
     * one.
     */
    protected void incrementDepthLimit() {
        currDepthLimit++;
    }

    /**
     * Primitive operation which is used to stop iterative deepening search in
     * situations where a clear best action exists. This implementation returns
     * always false.
     */
    protected boolean isSignificantlyBetter(double newUtility, double utility) {
        return false;
    }

    /**
     * Primitive operation which is used to stop iterative deepening search in
     * situations where a safe winner has been identified. This implementation
     * returns true if the given value (for the currently preferred action
     * result) is the highest or lowest utility value possible.
     */
    protected boolean hasSafeWinner(double resultUtility) {
        return resultUtility <= utilMin || resultUtility >= utilMax;
    }

    /**
     * Primitive operation, which estimates the value for (not necessarily
     * terminal) states. This implementation returns the utility value for
     * terminal states and <code>(utilMin + utilMax) / 2</code> for non-terminal
     * states. When overriding, first call the super implementation!
     */
    protected double eval(S state, P player) {
        if (game.isTerminal(state)) {
            return game.getUtility(state, player);
        } else {
            heuristicEvaluationUsed = true;
            return (utilMin + utilMax) / 2;
        }
    }

    /**
     * Primitive operation for action ordering. This implementation preserves
     * the original order (provided by the game).
     */
    public List<A> orderActions(S state, List<A> actions, P player, int depth) {
        return actions;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // nested helper classes

    private static class Timer {
        private long duration;
        private long startTime;

        Timer(int maxSeconds) {
            this.duration = 1000 * maxSeconds;
        }

        void start() {
            startTime = System.currentTimeMillis();
        }

        boolean timeOutOccurred() {
            return System.currentTimeMillis() > startTime + duration;
        }
    }

    /**
     * Orders actions by utility.
     */
    private static class ActionStore<A> {
        private List<A> actions = new ArrayList<>();
        private List<Double> utilValues = new ArrayList<>();

        void add(A action, double utilValue) {
            int idx = 0;
            while (idx < actions.size() && utilValue <= utilValues.get(idx))
                idx++;
            actions.add(idx, action);
            utilValues.add(idx, utilValue);
        }

        int size() {
            return actions.size();
        }
    }

    private class SearchPar {

        private S state;
        private A action;
        private P player;

        public SearchPar(S state, A action, P player) {
            this.state = state;
            this.action = action;
            this.player = player;
        }

        public P getPlayer() {
            return player;
        }

        public void setPlayer(P player) {
            this.player = player;
        }

        public S getState() {
            return state;
        }

        public void setState(S state) {
            this.state = state;
        }

        public A getAction() {
            return action;
        }

        public void setAction(A action) {
            this.action = action;
        }
    }

    private class ParallelComputation implements Runnable {

        /*
        private List<A> results;
        S state;
        P player;
            */

        private Optional<SearchPar> searchPar = Optional.empty();
        private Optional<Double> result = Optional.empty();
        private Optional<Runnable> callback = Optional.empty();
        private Semaphore semaphore = new Semaphore(0);
        private Boolean running = false;
        private Boolean stop = false;

        public ParallelComputation(/*List<A> results, S state, P player*/) {
            /*this.results=results;
            this.player=player;
            this.state=state;*/
        }

        public double getResult() {
            if (running) throw new IllegalStateException("Il thread sta ancora eseguendo");
            if (!result.isPresent()) throw new IllegalStateException("Non è stato generato un risultato");
            return result.get();
        }

        public void startSearch(SearchPar search, Runnable call) {
            if (running) throw new IllegalStateException("Il thread è già in esecuzione");

            searchPar = Optional.of(search);
            callback = Optional.of(call);
            running = true;
            result = Optional.empty();
            // faccio partire il thread
            semaphore.release();
        }

        private void stopWork() {
            stop = true;
            semaphore.release();
        }

        public void run() {
            /*
             * 1) Mi attivo e aspetto di ricevere un ramo
             * 2) Calcolo il ramo fino alla fine
             * 3) Una volta finito, setto una variabile, avverto il main e attendo un nuovo ordine
             */
            System.out.println("Thread start");
            // aspetto un comando
            while (!stop) {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Il thread è stato interrotto", e);
                }

                if (!stop) {
                    // calcolo minMax
                    //System.out.println("Inizio a calcolare");
                    result = Optional.of(minValue(game.getResult(searchPar.get().getState(), searchPar.get().getAction()), searchPar.get().player, Double.NEGATIVE_INFINITY,
                            Double.POSITIVE_INFINITY, 1));
                    running = false;
                    //System.out.println("Fine calcolo");
                    callback.get().run();
                }
            }
        }
    }

}
