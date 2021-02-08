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
    private int _batchIndex = 0;
    private PreparedStatement _preparedStatement;
    private List<HeaderProperties> _headerProperties;

    public static JdbcOntologyConsumer fromParams(JdbcOntologyConsumerParams params) {
        JdbcOntologyConsumer result = new JdbcOntologyConsumer();
        result._params = params;
        Util.loadDriver(result._params.getDriverClassName());
        result.setConnection();
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
            // batches of queries.
            try {
                _connection.setAutoCommit(false);
            } catch (SQLException throwables) {
                LOGGER.error("Error setting autocommit to false", throwables);
            }
        }
    }

    private void setPreparedStatement() {
        _headerProperties = Util.getHeaderPropertiesWithDocumentIdOverride(_params.getDocumentIdColAndDatatype());
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
        if (!tableExists(_params.getTable())) {
            createAnnotationTable();
        }
        // Now that the table is created, you can prepare the statement
        // Note: If you try to prepare the statement beforehand, it COULD result in a
        // null prepared statement and then you will get a null pointer exception
        setPreparedStatement();
    }

    public boolean tableExists(String table) {
        // TODO: Is there a better way to do this?
        // TODO: Replace w/ prepared statement?
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
        String query = Util.getCreateTableQuery(_params.getTable(), _params.getCreateTableSuffix(), _params.getDocumentIdColAndDatatype());
        executeUpdate(query);
    }

    private int executeUpdate(String query) {
        LOGGER.info("Executing update: " + query);
        int result = 0;
        try {
            Statement statement = _connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            result = statement.executeUpdate(query);
            commit();
            statement.close(); // Close statement since you are not returning any resultSet
        } catch (SQLException throwables) {
            LOGGER.error("Error executing update", throwables);
        }
        return result;
    }

    @Override
    public void insertOntologyIntoAnnotationTable(Ontology ontology) {
        setParametersForPreparedStatement(ontology);
        try {
            if (_supportsBatchUpdates && _params.getBatchSize() > 1) {
                _preparedStatement.addBatch();
                ++_batchIndex;
                if (_batchIndex >= _params.getBatchSize()) {
                    LOGGER.info("Executing batch update for ontologies");
                    _batchIndex = 0;
                    executeBatch();
                }
            } else {
                LOGGER.info("Executing update for ontology");
                _preparedStatement.executeUpdate();
                commit();
            }
        } catch (SQLException e) {
            LOGGER.error("Error inserting ontology into table", e);
        }
    }

    private void executeBatch() {
        try {
            _preparedStatement.executeBatch();
            commit();
        } catch (SQLException throwables) {
            LOGGER.error("Error executing batch", throwables);
        }
    }

    private void setParametersForPreparedStatement(Ontology ontology) {
        try {
            int i = 1;
            for (HeaderProperties p : _headerProperties) {
                String column = p.getName();
                switch (column) {
                    case Const.IDENTIFIED_ANNOTATION_ADDRESS_HEADER:
                        setVal(i, ontology.getIdentifiedAnnotationAddress(), Types.INTEGER);
                        break;
                    case Const.CODE_HEADER:
                        setVal(i, ontology.getCode(), Types.VARCHAR);
                        break;
                    case Const.CONDITIONAL_HEADER:
                        setVal(i, ontology.getConditional(), Types.INTEGER);
                        break;
                    case Const.CUI_HEADER:
                        setVal(i, ontology.getCui(), Types.VARCHAR);
                        break;
                    case Const.DOCUMENT_ID_HEADER:
                        setVal(i, ontology.getDocumentId(), Types.VARCHAR);
                        break;
                    case Const.GENERIC_HEADER:
                        setVal(i, ontology.getGeneric(), Types.INTEGER);
                        break;
                    case Const.POLARITY_HEADER:
                        setVal(i, ontology.getPolarity(), Types.INTEGER);
                        break;
                    case Const.END_HEADER:
                        setVal(i, ontology.getEnd(), Types.INTEGER);
                        break;
                    case Const.BEGIN_HEADER:
                        setVal(i, ontology.getBegin(), Types.INTEGER);
                        break;
                    case Const.PREFERRED_TEXT_HEADER:
                        setVal(i, ontology.getPreferredText(), Types.VARCHAR);
                        break;
                    case Const.REFSEM_HEADER:
                        setVal(i, ontology.getRefsem(), Types.VARCHAR);
                        break;
                    case Const.SCHEME_HEADER:
                        setVal(i, ontology.getCodingScheme(), Types.VARCHAR);
                        break;
                    case Const.SCORE_HEADER:
                        setVal(i, ontology.getScore(), Types.DOUBLE);
                        break;
                    case Const.SUBJECT_HEADER:
                        setVal(i, ontology.getSubject(), Types.VARCHAR);
                        break;
                    case Const.TEXTSEM_HEADER:
                        setVal(i, ontology.getTextsem(), Types.VARCHAR);
                        break;
                    case Const.TUI_HEADER:
                        setVal(i, ontology.getTui(), Types.VARCHAR);
                        break;
                    case Const.UNCERTAINTY_HEADER:
                        setVal(i, ontology.getUncertainty(), Types.INTEGER);
                        break;
                    case Const.TRUE_TEXT_HEADER:
                        setVal(i, ontology.getTrueText(), Types.VARCHAR);
                        break;
                    case Const.PARTS_OF_SPEECH_HEADER:
                        setVal(i, ontology.getPartsOfSpeech(), Types.VARCHAR);
                        break;
                    case Const.ENTITY_TYPE_HEADER:
                        setVal(i, ontology.getEntityType(), Types.VARCHAR);
                        break;
                    case Const.SEGMENT_HEADER:
                        setVal(i, ontology.getSegment(), Types.VARCHAR);
                        break;
                    case Const.DISCOVERY_TECHNIQUE_HEADER:
                        setVal(i, ontology.getDiscoveryTechnique(), Types.VARCHAR);
                        break;
                    case Const.HISTORY_OF_HEADER:
                        setVal(i, ontology.getHistoryOf(), Types.INTEGER);
                        break;
                    case Const.OID_HEADER:
                        setVal(i, ontology.getOid(), Types.VARCHAR);
                        break;
                    case Const.OUI_HEADER:
                        setVal(i, ontology.getOui(), Types.VARCHAR);
                        break;
                    case Const.DISAMBIGUATED_HEADER:
                        setVal(i, ontology.getDisambiguated(), Types.INTEGER);
                        break;
                    case Const.ONTOLOGY_ADDRESS_HEADER:
                        setVal(i, ontology.getOntologyConceptAddress(), Types.INTEGER);
                        break;
                    default: {
                        HeaderProperties documentIdOverride = _params.getDocumentIdColAndDatatype();
                        if (documentIdOverride != null &&
                            p.getName() != null &&
                            p.getName().equals(documentIdOverride.getName())) {
                            // special case for document id column
                            String datatype = documentIdOverride.getDataType().toLowerCase();
                            if (datatype.contains("int")) {
                                setVal(i, Integer.valueOf(ontology.getDocumentId()), Types.INTEGER);
                            } else if (datatype.contains("decimal") || datatype.contains("numeric")) {
                                setVal(i, Double.valueOf(ontology.getDocumentId()), Types.DOUBLE);
                            } else {
                                setVal(i, ontology.getDocumentId(), Types.VARCHAR);
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

    private <T> void setVal(int i, T val, int type) {
        try {
            if (val == null) {
                _preparedStatement.setNull(i, type);
            } else {
                if (val instanceof Integer) {
                    _preparedStatement.setInt(i, (Integer)val);
                } else if (val instanceof Double) {
                    _preparedStatement.setDouble(i, (Double)val);
                } else if (val instanceof Float) {
                    _preparedStatement.setFloat(i, (Float) val);
                } else if (val instanceof String) {
                    _preparedStatement.setString(i, (String)val);
                } else {
                    _preparedStatement.setNull(i, type);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error setting value " + val, e);
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
        try {
            if (_preparedStatement != null) {
                if (_supportsBatchUpdates && _batchIndex > 0) {
                    executeBatch();
                }
                _preparedStatement.close();
            }
            _connection.close();
        } catch (SQLException throwables) {
            LOGGER.error("Error closing connection to teradata", throwables);
        }
    }
}
