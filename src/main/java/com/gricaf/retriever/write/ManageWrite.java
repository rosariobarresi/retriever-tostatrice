package com.gricaf.retriever.write;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ManageWrite {

    File addDataToFile(List<String> headers, List<Object> row, String nameSheet, String fileLocation) throws IOException;
}
