/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opengdc.parser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import opengdc.GUI;
import opengdc.util.FSUtils;
import opengdc.util.FormatUtils;

/**
 *
 * @author fabio
 */
public class MethylationBetaValueParser extends BioParser {

    @Override
    public int convert(String program, String disease, String dataType, String inPath, String outPath) {
        int acceptedFiles = FSUtils.acceptedFilesInFolder(inPath, getAcceptedInputFileFormats());
        System.err.println("Data Amount: " + acceptedFiles + " files" + "\n\n");
        GUI.appendLog("Data Amount: " + acceptedFiles + " files" + "\n\n");
        
        if (acceptedFiles == 0)
            return 1;
        
        HashSet<String> filesPathConverted = new HashSet<>();
        
        File[] files = (new File(inPath)).listFiles();
        for (File f: files) {
            if (f.isFile()) {
                String extension = FSUtils.getFileExtension(f);
                if (getAcceptedInputFileFormats().contains(extension)) {
                    System.err.println("Processing " + f.getName());
                    GUI.appendLog("Processing " + f.getName() + "\n");
                    
                    String uuid = f.getName().split("_")[0];
                    try {
                        Files.write((new File(outPath + uuid + "." + this.getFormat())).toPath(), (FormatUtils.initDocument(this.getFormat())).getBytes("UTF-8"), StandardOpenOption.CREATE);
                        
                        InputStream fstream = new FileInputStream(f.getAbsolutePath());
                        DataInputStream in = new DataInputStream(fstream);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String line;
                        boolean firstLine = true;
                        while ((line = br.readLine()) != null) {
                            if (firstLine)
                                firstLine = false; // just skip the first line (header)
                            else {
                                String[] line_split = line.split("\t");
                                String chr = line_split[2];
                                String start = line_split[3];
                                String end = line_split[4];
                                String strand = "*"; //retrieve strande from NCBI
                                String composite_element_ref = line_split[0];
                                String beta_value = line_split[1];
                                String gene_symbol = line_split[5];
                                String gene_type = line_split[6];
                                String transcript_id = line_split[7];
                                String position_to_tss = line_split[8];
                                String cgi_coordinate = line_split[9];
                                String feature_type = line_split[10];

                                ArrayList<String> values = new ArrayList<>();
                                values.add(chr);
                                values.add(start);
                                values.add(end);
                                values.add(strand);
                                values.add(composite_element_ref);
                                values.add(beta_value);
                                values.add(gene_symbol);
                                values.add(gene_type);
                                values.add(transcript_id);
                                values.add(position_to_tss);
                                values.add(cgi_coordinate);
                                values.add(feature_type);

                                Files.write((new File(outPath + uuid + "." + this.getFormat())).toPath(), (FormatUtils.createEntry(this.getFormat(), values, getHeader())).getBytes("UTF-8"), StandardOpenOption.APPEND);
                            }
                        }
                        br.close();
                        in.close();
                        fstream.close();
                        
                        Files.write((new File(outPath + uuid + "." + this.getFormat())).toPath(), (FormatUtils.endDocument(this.getFormat())).getBytes("UTF-8"), StandardOpenOption.APPEND);
                        filesPathConverted.add(outPath + uuid + "." + this.getFormat());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        if (!filesPathConverted.isEmpty()) {
            // write header.schema
            try {
                System.err.println("\n" + "Generating header.schema");
                GUI.appendLog("\n" + "Generating header.schema" + "\n");
                Files.write((new File(outPath + "header.schema")).toPath(), (FormatUtils.generateDataSchema(this.getHeader(), this.getAttributesType())).getBytes("UTF-8"), StandardOpenOption.CREATE);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return 0;
    }

    @Override
    public String[] getHeader() {
        String[] header = new String[12];
        header[0] = "chr";
        header[1] = "start";
        header[2] = "stop";
        header[3] = "strand";
        header[4] = "composite_element_ref";
        header[5] = "beta_value";
        header[6] = "gene_symbol";
        header[7] = "gene_type";
        header[8] = "transcript_id";
        header[9] = "position_to_tss";
        header[10] = "cgi_coordinate";
        header[11] = "feature_type";
        return header;
    }

    @Override
    public String[] getAttributesType() {
        String[] attr_type = new String[12];
        attr_type[0] = "STRING";
        attr_type[1] = "LONG";
        attr_type[2] = "LONG";
        attr_type[3] = "CHAR";
        attr_type[4] = "STRING";
        attr_type[5] = "FLOAT";
        attr_type[6] = "STRING";
        attr_type[7] = "STRING";
        attr_type[8] = "STRING";
        attr_type[9] = "STRING";
        attr_type[10] = "STRING";
        attr_type[11] = "STRING";
        return attr_type;
    }

    @Override
    public void initAcceptedInputFileFormats() {
        this.acceptedInputFileFormats = new HashSet<>();
        this.acceptedInputFileFormats.add(".txt");
    }
    
}
