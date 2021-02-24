package eci.arsw.covidanalyzer;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The type Covid analyzer thread.
 */
public class CovidAnalyzerThread  extends Thread{

    private ResultAnalyzer resultAnalyzer;
    private TestReader testReader;
    private AtomicInteger amountOfFilesProcessed;
    private List<File> covidFiles;
    private boolean pause = true, firstRun=true;

    /**
     * Instantiates a new Covid analyzer thread.
     *
     * @param resultAnalyzer         the result analyzer
     * @param testReader             the test reader
     * @param amountOfFilesProcessed the amount of files processed
     * @param covidFiles             the covid files
     */
    public CovidAnalyzerThread(ResultAnalyzer resultAnalyzer, TestReader testReader, AtomicInteger amountOfFilesProcessed, List<File> covidFiles) {
        this.resultAnalyzer = resultAnalyzer;
        this.testReader = testReader;
        this.amountOfFilesProcessed = amountOfFilesProcessed;
        this.covidFiles = covidFiles;
    }

    /**
     * Unpause.
     */
    public synchronized void unpause() {
        this.pause = true;
        this.notify();
    }

    /**
     * Pause.
     */
    public void pause() {
        this.pause = false;

    }

    /**
     * Is paused boolean.
     *
     * @return the boolean
     */
    public boolean isPaused(){
        return pause;
    }

    /**
     * Is first run boolean.
     *
     * @return the boolean
     */
    public boolean isFirstRun() {
        return firstRun;
    }

    /**
     * Sets first run.
     *
     * @param firstRun the first run
     */
    public void setFirstRun(boolean firstRun) {
        this.firstRun = firstRun;
    }

    public void run(){
        int contador = 0;
        while(contador<covidFiles.size()){
            while (!pause) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (File resultFile : covidFiles) {
                List<Result> results = testReader.readResultsFromFile(resultFile);
                for (Result result : results) {
                    resultAnalyzer.addResult(result);
                }
                amountOfFilesProcessed.incrementAndGet();
                contador++;
            }

        }



    }

}
