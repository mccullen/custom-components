package icapa.cr;

import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.log4j.Logger;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;

public class StressTestReader extends JCasCollectionReader_ImplBase {
    static private final Logger LOGGER = Logger.getLogger(StressTestReader.class.getName());

    static public final String PARAM_N_DOCUMENTS = "NDocuments";
    @ConfigurationParameter(
        name = PARAM_N_DOCUMENTS,
        description = "Number of test documents to create"
    )
    private int _nDocuments;

    private int _currentDocument = 0;

    @Override
    public void getNext(JCas jCas) {
        String text = "Abstract:Cereblon (CRBN), a substrate receptor of the E3 ubiquitin ligase complex CRL4CRBN, is the target of theimmunomodulatory drugs lenalidomide and pomalidomide. Recently, it was demonstrated that binding of thesedrugs to CRBN promotes the ubiquitination and subsequent degradation of two common substrates, transcriptionfactors Aiolos and Ikaros. Here we report that the pleiotropic pathway modifier CC-122, a new chemical entitytermed pleiotropic pathway modifier binds CRBN and promotes degradation of Aiolos and Ikaros in diffuse largeB-cell lymphoma (DLBCL) and T cells in vitro, in vivo and in patients, resulting in both cell autonomous as well asimmunostimulatory effects. In DLBCL cell lines, CC-122-induced degradation or shRNA mediated knockdown ofAiolos and Ikaros correlates with increased transcription of interferon stimulated genes (ISGs) independent ofinterferon production and/or secretion and results in apoptosis in both ABC and GCB-DLBCL cell lines. Ourresults provide mechanistic insight into the cell of origin independent anti-lymphoma activity of CC-122, in contrastto the ABC subtype selective activity of lenalidomide.";
        jCas.setDocumentText(text);
        DocumentID documentID = new DocumentID(jCas);
        documentID.setDocumentID(String.valueOf(_currentDocument));
        documentID.addToIndexes();
        ++_currentDocument;
    }

    @Override
    public boolean hasNext() {
        return _currentDocument < _nDocuments;
    }

    @Override
    public Progress[] getProgress() {
        return new Progress[0];
    }
}
