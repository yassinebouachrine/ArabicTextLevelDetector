package com.arabictextcomplexity;

import com.arabictextcomplexity.data.DatasetLoader;
import com.arabictextcomplexity.model.ComplexityModelTrainer;
import com.arabictextcomplexity.model.ComplexityModelTrainer.TrainedModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;

import java.util.List;

public class ComplexityModelBuilder {
    private static final Logger log = LoggerFactory.getLogger(ComplexityModelBuilder.class);

    public static void main(String[] args) {
        try {
            String root = System.getProperty("user.dir");
            String csv = root + "/src/main/resources/data/arabic_text_level.csv";
            String arff = root + "/src/main/resources/data/features.arff";
            String modelFile = root + "/src/main/resources/models/arabic.model";

            log.info("Loading raw dataset...");
            DatasetLoader loader = new DatasetLoader();
            var list = loader.loadDataset(csv);
            Instances data = loader.convertToWekaInstances(list);
            loader.saveAsArff(data, arff);

            ComplexityModelTrainer trainer = new ComplexityModelTrainer();

            log.info("Training models...");
            List<TrainedModel> models = trainer.trainAll(data);

            log.info("=== STATISTIQUES DES MODÈLES ===");
            for (TrainedModel model : models) {
                log.info(model.getStatisticsSummary());
            }

            TrainedModel best = trainer.train(data);
            log.info("Meilleur modèle: [{}] avec une précision de {}%", best.getType(), best.getAccuracy());

            log.info("Saving model to {}...", modelFile);
            trainer.saveModel(best.getModel(), modelFile);
            log.info("Done.");

        } catch (Exception e) {
            log.error("Error building model", e);
            e.printStackTrace();
        }
    }
}