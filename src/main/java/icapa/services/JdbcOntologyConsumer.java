package icapa.services;

import icapa.Const;
import icapa.Util;
import icapa.models.HeaderProperties;
import icapa.models.JdbcOntologyConsumerParams;
import icapa.models.Ontology;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.List;

public class JdbcOntologyConsumer implements OntologyConsumer {
    private static final Logger LOGGER = Logger.getLogger(JdbcOntologyConsumer.class.getName());

    private JdbcOntologyConsumerParams _params;
    private Connection _connection;
    private boolean _supportsBatchUpdates = false;
    private int _nBatches = 0;
    private PreparedStatement _preparedStatement;
    private List<HeaderProperties> _headerProperties;

    public static JdbcOntologyConsumer fromParams(JdbcOntologyConsumerParams params) {
        JdbcOntologyConsumer result = new JdbcOntologyConsumer();
        result._params = params;
        Util.loadDriver(result._params.getDriverClassName());
        result.setConnection();
        result.setPreparedStatement();
        return result;
    }

    private void setConnection() {
        if (_params.getUsername() != null && _params.getPassword() != null) {
            _connection = Util.getConnection(
                _params.getUsername(),
                _params.getPassword(),
                _params.getUrl());
        } else {
            _connection = Util.getConnection(_params.getUrl());
        }
        setSupportsBatchUpdates();
        if (_supportsBatchUpdates) {
            // Batch updates supported, so set autocommit to false so you have to explicitly commit
            // batches of queries. Also, create the batch statement that you will add batch queries to.
            try {
                _connection.setAutoCommit(false);
                _batchStatement = _connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                //_connection.prepareStatement("",ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            } catch (SQLException throwables) {
                LOGGER.error("Error setting autocommit to false", throwables);
            }
        }
    }

    private void setPreparedStatement() {
        _headerProperties = Util.getHeaderPropertiesWithDocumentIdOverride(_params.getDocumentIdColAndDatetype());
        String template = Util.getInsertTemplate(_params.getTable(), _headerProperties);
        try {
            _preparedStatement = _connection.prepareStatement(template, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch (SQLException throwables) {
            LOGGER.error("Error preparing statement " + template, throwables);
        }
    }

    private void setSupportsBatchUpdates() {
        try {
            _supportsBatchUpdates = _connection.getMetaData().supportsBatchUpdates();
        } catch (SQLException throwables) {
            LOGGER.error(throwables);
        }
        LOGGER.info("Driver supports batch updates: " + _supportsBatchUpdates);
    }

    @Override
    public void createAnnotationTableIfAbsent() {
        if (tableExists(_params.getTable())) {
            createAnnotationTable();
        }
    }

    public boolean tableExists(String table) {
        // TODO: Is there a better way to do this?
        boolean result = false;
        try {
            Statement statement = _connection.createStatement();
            statement.execute("SELECT 1 FROM " + table);
            commit();
            statement.close();
            result = true;
        } catch (SQLException throwables) {
            LOGGER.info("Table " + table + " does not exist. Attempting to create");
        }
        return result;
    }

    private void createAnnotationTable() {

    }

    @Override
    public void insertOntologyIntoAnnotationTable(Ontology ontology) {
        if (_supportsBatchUpdates && _params.getBatchSize() > 1) {
            // TODO: Do batch update using prepared statement
            setParametersForPreparedStatement(ontology);
        } else {
        }
    }

    private void setParametersForPreparedStatement(Ontology ontology) {
        try {
            int i = 1;
            for (HeaderProperties p : _headerProperties) {
                String datatype = p.getDataType().toLowerCase();
                String column = p.getName();
                switch (column) {
                    case Const.IDENTIFIED_ANNOTATION_ADDRESS_HEADER:
                        _preparedStatement.setInt(i, ontology.getIdentifiedAnnotationAddress());
                        break;
                    case Const.CODE_HEADER:
                        _preparedStatement.setString(i, ontology.getCode());
                        break;
                    case Const.CONDITIONAL_HEADER:
                        _preparedStatement.setInt(i, ontology.getConditional() ? 1 : 0);
                        break;
                    case Const.CUI_HEADER:
                        _preparedStatement.setString(i, ontology.getCui());
                        break;
                    case Const.DOCUMENT_ID_HEADER:
                        _preparedStatement.setString(i, ontology.getDocumentId());
                        break;
                    case Const.GENERIC_HEADER:
                        break;
                    case Const.POLARITY_HEADER:
                        break;
                    case Const.END_HEADER:
                        break;
                    case Const.BEGIN_HEADER:
                        break;
                    case Const.PREFERRED_TEXT_HEADER:
                        break;
                    case Const.REFSEM_HEADER:
                        break;
                    case Const.SCHEME_HEADER:
                        break;
                    case Const.SCORE_HEADER:
                        break;
                    case Const.SUBJECT_HEADER:
                        break;
                    case Const.TEXTSEM_HEADER:
                        break;
                    case Const.TUI_HEADER:
                        break;
                    case Const.UNCERTAINTY_HEADER:
                        break;
                    case Const.TRUE_TEXT_HEADER:
                        break;
                    case Const.PARTS_OF_SPEECH_HEADER:
                        break;
                    case Const.ENTITY_TYPE_HEADER:
                        break;
                    case Const.SEGMENT_HEADER:
                        break;
                    case Const.DISCOVERY_TECHNIQUE_HEADER:
                        break;
                    case Const.HISTORY_OF_HEADER:
                        break;
                    case Const.OID_HEADER:
                        break;
                    case Const.OUI_HEADER:
                        break;
                    case Const.DISAMBIGUATED_HEADER:
                        break;
                    case Const.ONTOLOGY_ADDRESS_HEADER:
                        break;
                    default: {
                        HeaderProperties documentIdOverride = _params.getDocumentIdColAndDatatype();
                        if (documentIdOverride != null &&
                            p.getName() != null &&
                            p.getName().equals(documentIdOverride.getName())) {
                            // special case for document id column
                            String datatype = documentIdOverride.getDataType().toLowerCase();
                            if (datatype.contains("int")) {
                                _preparedStatement.setInt(i, Integer.valueOf(ontology.getDocumentId()));
                            } else if (datatype.contains("decimal") || datatype.contains("numeric")) {
                                _preparedStatement.setDouble(i, Double.valueOf(ontology.getDocumentId()));
                            } else {
                                _preparedStatement.setString(i, ontology.getDocumentId());
                            }
                        }
                        break;
                    }
                }
                ++i;
            }
        } catch (Exception e) {
            LOGGER.error("Error setting parameters for prepared statement", e);
        }
    }

    private void commit() {
        try {
            if (!_connection.getAutoCommit()) {
                _connection.commit();
            }
        } catch (SQLException throwables) {
            LOGGER.error("Error committing: ", throwables);
        }
    }

    @Override
    public void close() {
    }
}
