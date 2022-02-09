package icapa.services;

import icapa.models.Recommendation;

public interface RecommendationWriterService {
    void writeHeaderLine();
    void writeRecommendationLine(Recommendation recommendation);
}
