package eci.arsw.covidanalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Camel Application
 */
public class CovidAnalyzerTool {

    private ResultAnalyzer resultAnalyzer;
    private TestReader testReader;
    private int amountOfFilesTotal;
    private AtomicInteger amountOfFilesProcessed;
    private ArrayList<CovidAnalyzerThread> covidAnalyzerThreads;
    public static final int NUMBER_THREADS = 5;


    public CovidAnalyzerTool() {
        resultAnalyzer = new ResultAnalyzer();
        testReader = new TestReader();
        amountOfFilesProcessed = new AtomicInteger();
        covidAnalyzerThreads = new ArrayList<>();
    }

    private List<File> getResultFileList() {
        List<File> csvFiles = new ArrayList<>();
        try (Stream<Path> csvFilePaths = Files.walk(Paths.get("src/main/resources/")).filter(path -> path.getFileName().toString().endsWith(".csv"))) {
            csvFiles = csvFilePaths.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFiles;
    }


    public Set<Result> getPositivePeople() {
        return resultAnalyzer.listOfPositivePeople();
    }




    private void covidAnalyzerThreadsInit() {
        amountOfFilesProcessed.set(0);
        List<File> resultFiles = getResultFileList();
        amountOfFilesTotal = resultFiles.size();
        int amountOfFileByThread = amountOfFilesTotal/NUMBER_THREADS;
        for (int i = 0; i < NUMBER_THREADS; i++) {
            int aux = i+1==NUMBER_THREADS?amountOfFilesTotal%NUMBER_THREADS:0;
            List<File> covidFiles = resultFiles.subList(i*amountOfFileByThread, (i+1)*amountOfFileByThread+aux);
            CovidAnalyzerThread covidAnalyzerThread = new CovidAnalyzerThread(resultAnalyzer, testReader, amountOfFilesProcessed, covidFiles);
            covidAnalyzerThreads.add(covidAnalyzerThread);

        }
    }

    private static void esperaSegura() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        CovidAnalyzerTool covidAnalyzerTool = new CovidAnalyzerTool();
        covidAnalyzerTool.covidAnalyzerThreadsInit();

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            if(line.contains("exit")){
                break;
            }
            if(line.contains("")){
                esperaSegura();
                for (CovidAnalyzerThread t :covidAnalyzerTool.covidAnalyzerThreads) {
                    if(t.isPaused() && !t.isFirstRun()){
                        t.pause();
                        System.out.println("Threads Paused.");
                    }else{
                        if(t.isFirstRun()){
                            t.setFirstRun(false);
                            t.start();
                        }else{
                            t.unpause();
                        }
                        System.out.println("Threads Running...");

                    }
                }
            }
            String message = "Processed %d out of %d files.\nFound %d positive people:\n%s";
            Set<Result> positivePeople = covidAnalyzerTool.getPositivePeople();
            String affectedPeople = positivePeople.stream().map(Result::toString).reduce("", (s1, s2) -> s1 + "\n" + s2);
            message = String.format(message, covidAnalyzerTool.amountOfFilesProcessed.get(), covidAnalyzerTool.amountOfFilesTotal, positivePeople.size(), affectedPeople);
            System.out.println(message);
        }
    }

}

