package com.gricaf.retriever.jobscheduling.job;

import com.gricaf.retriever.write.ManageCsv;
import com.gricaf.retriever.write.ManageExcel;
import org.apache.commons.io.FileUtils;
import org.eclipse.milo.examples.client.MappaValoriSingleton;
import org.eclipse.milo.examples.client.Subscription;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author pratikdas
 */
@Service
public class Retriever {

    @Autowired
    public ManageExcel manageExcel;

    @Autowired
    public ManageCsv manageCsv;
    @Value("${scheduler.filename}")
    private String fileName;
    @Value("${scheduler.destfolder}")
    private String destFolder;
    @Value("${scheduler.useexcel}")
    private boolean useexcel;

    @Value("${scheduler.nodi}")
    private List<String> nodiToRead;

    @Value("${scheduler.endpointurls}")
    private List<String> urls;

    static final Logger LOGGER = Logger.getLogger(Retriever.class.getName());

    //    @Scheduled(cron = "${scheduler.interval-in-cron}")
//    @Async
    public void retrieveData() throws IOException {
        LOGGER.info("inizio recupero dati " + new Date());

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
//        String fileLocation = path.substring(0, path.length() - 1) + fileName;
        String fileLocation = fileName;

        File file = null;
        for (String url : urls) {
            boolean connessione = false;
            for (int count = 0; count < 3 && !connessione; count++) {
                try {
                    retrieveNodi(url);
                    connessione = true;
                } catch (Throwable e) {
                    e.printStackTrace();
                    LOGGER.info("errore connessione");
                    LOGGER.info(e.getMessage());
                }
            }
            if (useexcel) {
                file = writeUpdateExcel(fileLocation, url);
            } else {
                file = writeFile(fileLocation, url);
                moveFileExcel(file);
            }
            MappaValoriSingleton.getIstance().reset();

        }
        if (useexcel) {
            moveFileExcel(file);
        }
    }

    private File writeFile(String fileLocation, String url) throws IOException {
        List<Object> values = createListOfValueToWrite();
        url = url.replace(":", "").replace("/", "");
        LOGGER.info("inizio scrittura su file " + fileName + " sheet " + url);
        List<String> chiaviNodi = nodiToRead.stream()
                .map(s -> s.split("#")[1])
                .collect(Collectors.toList());
        File file = manageCsv.addDataToFile(chiaviNodi, values, url, fileLocation);
        LOGGER.info("fine scrittura su file " + fileName + " sheet " + url);
        return file;
    }

    public void moveFileExcel(File source) {
        File dest = new File(destFolder);
        try {
            FileUtils.copyFileToDirectory(source, dest);
            LOGGER.info("file copiato nella cartella " + destFolder);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HHmm");
//            String destTime = source.getName()+" "+sdf.format(new Date());
            String destTime = source.getName().substring(0, source.getName().lastIndexOf(".")) + " " + sdf.format(new Date()) + source.getName().substring(source.getName().lastIndexOf("."));
            Path fileDestinazione = Paths.get(destFolder + "\\" + source.getName());
            Files.move(fileDestinazione, fileDestinazione.resolveSibling(destTime));
            LOGGER.info("file rinominato in  " + fileDestinazione.resolveSibling(destTime));
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Retriever r = new Retriever();
        r.destFolder = "C:\\Users\\RosarioBarresi\\Documents";
        r.moveFileExcel(new File("dati_macchinette opc.tcplocalhost12686milo.csv"));
    }

    private File writeUpdateExcel(String fileLocation, String url) {
        List<Object> values = createListOfValueToWrite();
        url = url.replace(":", "").replace("/", "");
        LOGGER.info("inizio scrittura su file " + fileName + " sheet " + url);
        File file = null;
        try {
            file = manageExcel.addDataToFile(new ArrayList<>(MappaValoriSingleton.getIstance().keySet()), values, url, fileLocation);
        } catch (
                IOException e) {
            LOGGER.info("errore scrittura file");
            LOGGER.info(e.getMessage());
            e.printStackTrace();
        }
        LOGGER.info("fine scrittura su file " + fileName + " sheet " + url);
        return file;
    }

    private static List<Object> createListOfValueToWrite() {
        List<Object> values = new ArrayList<>();
        LOGGER.info("chiavi trovate " + MappaValoriSingleton.getIstance());
        for (String key : MappaValoriSingleton.getIstance().keySet()) {
            String value = MappaValoriSingleton.getIstance().getValue(key);
            if (value == null) {
                values.add(-1);
            } else if (value.contains(".") || value.contains(",")) {
                values.add(Double.parseDouble(value));
            } else if (value.matches("[0-9]+")) {
                values.add(Integer.parseInt(value));
            } else {
                values.add(-1);
            }
        }
        LOGGER.info("valori trovati " + values);
        return values;
    }

    private void retrieveNodi(String url) throws Exception {
        Subscription subscription = new Subscription();
        Subscription.endpointUrl = url;
        LOGGER.info("connessione a url " + subscription.getEndpointUrl());
        List<NodeId> nodi = new ArrayList<>();
        for (String nodo : nodiToRead) {
            String[] split = nodo.split("#");
            nodi.add(new NodeId(Integer.parseInt(split[0]), split[1]));
        }

        subscription.recuperoDati(nodi);
    }


}