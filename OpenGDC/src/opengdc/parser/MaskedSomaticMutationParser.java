/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opengdc.parser;

import opengdc.GUI;
import opengdc.util.FSUtils;
import opengdc.reader.MaskedSomaticMutationReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import opengdc.util.FormatUtils;

/**
 *
 * @author fabio
 */
public class MaskedSomaticMutationParser extends BioParser {

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
                    
                    HashSet<String> aliquot_uuids = MaskedSomaticMutationReader.getUUIDsFromMaf(f.getAbsolutePath());
                    for (String aliquot_uuid: aliquot_uuids) {
                        HashMap<Integer, HashMap<String, String>> uuidData = MaskedSomaticMutationReader.getUUIDDataFromMaf(f.getAbsolutePath(), aliquot_uuid);
                        //System.err.println("data: "+uuidData.size());
                        
                        if (!uuidData.isEmpty()) {
                            try {
                                if (!filesPathConverted.contains(outPath + aliquot_uuid + "." + this.getFormat())) {
                                    Files.write((new File(outPath + aliquot_uuid + "." + this.getFormat())).toPath(), (FormatUtils.initDocument(this.getFormat())).getBytes("UTF-8"), StandardOpenOption.CREATE);
                                    filesPathConverted.add(outPath + aliquot_uuid + "." + this.getFormat());
                                }
                            
                                for (int entry: uuidData.keySet()) {
                                    ArrayList<String> values = new ArrayList<>();
                                    values.add(uuidData.get(entry).get("chromosome"));
                                    values.add(uuidData.get(entry).get("start_position"));
                                    values.add(uuidData.get(entry).get("end_position"));
                                    values.add(uuidData.get(entry).get("strand"));
                                    values.add(uuidData.get(entry).get("hugo_symbol"));
                                    values.add(uuidData.get(entry).get("entrez_gene_id"));
                                    values.add(uuidData.get(entry).get("variant_classification"));
                                    values.add(uuidData.get(entry).get("variant_type"));
                                    values.add(uuidData.get(entry).get("reference_allele"));
                                    values.add(uuidData.get(entry).get("tumor_seq_allele1"));
                                    values.add(uuidData.get(entry).get("tumor_seq_allele2"));
                                    values.add(uuidData.get(entry).get("dbsnp_rs"));
                                    values.add(uuidData.get(entry).get("tumor_sample_barcode"));
                                    values.add(uuidData.get(entry).get("matched_norm_sample_barcode"));
                                    values.add(uuidData.get(entry).get("match_norm_seq_allele1"));
                                    values.add(uuidData.get(entry).get("match_norm_seq_allele2"));
                                    values.add(uuidData.get(entry).get("tumor_sample_uuid"));
                                    values.add(uuidData.get(entry).get("matched_norm_sample_uuid"));
                                    Files.write((new File(outPath + aliquot_uuid + "." + this.getFormat())).toPath(), (FormatUtils.createEntry(this.getFormat(), values, getHeader())).getBytes("UTF-8"), StandardOpenOption.APPEND);
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    
                }
            }
        }
        
        if (!filesPathConverted.isEmpty()) {
            // close documents
            for (String path: filesPathConverted) {
                try {
                    Files.write((new File(path)).toPath(), (FormatUtils.endDocument(this.getFormat())).getBytes("UTF-8"), StandardOpenOption.APPEND);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
        String[] header = new String[18];
        header[0] = "chr";
        header[1] = "start";
        header[2] = "stop";
        header[3] = "strand";
        header[4] = "hugo_symbol";
        header[5] = "entrez_gene_id";
        header[6] = "variant_classification";
        header[7] = "variant_type";
        header[8] = "reference_allele";
        header[9] = "tumor_seq_allele1";
        header[10] = "tumor_seq_allele2";
        header[11] = "dbsnp_rs";
        header[12] = "tumor_sample_barcode";
        header[13] = "matched_norm_sample_barcode";
        header[14] = "match_norm_seq_allele1";
        header[15] = "match_norm_seq_allele2";
        header[16] = "tumor_sample_uuid";
        header[17] = "matched_norm_sample_uuid";
        return header;
    }

    @Override
    public String[] getAttributesType() {
        String[] attr_type = new String[18];
        attr_type[0] = "STRING";
        attr_type[1] = "LONG";
        attr_type[2] = "LONG";
        attr_type[3] = "CHAR";
        attr_type[4] = "STRING";
        attr_type[5] = "STRING";
        attr_type[6] = "STRING";
        attr_type[7] = "STRING";
        attr_type[8] = "STRING";
        attr_type[9] = "STRING";
        attr_type[10] = "STRING";
        attr_type[11] = "STRING";
        attr_type[12] = "STRING";
        attr_type[13] = "STRING";
        attr_type[14] = "STRING";
        attr_type[15] = "STRING";
        attr_type[16] = "STRING";
        attr_type[17] = "STRING";
        return attr_type;
    }

    @Override
    public void initAcceptedInputFileFormats() {
        this.acceptedInputFileFormats = new HashSet<>();
        this.acceptedInputFileFormats.add(".maf");
    }
    
}
