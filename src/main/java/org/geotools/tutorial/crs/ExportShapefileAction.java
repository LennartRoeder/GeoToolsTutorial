package org.geotools.tutorial.crs;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

class ExportShapefileAction extends SafeAction {

    ExportShapefileAction() {
        super("Export...");
        putValue(Action.SHORT_DESCRIPTION, "Export using current crs");
    }
    public void action(ActionEvent e) throws Throwable {
        exportToShapefile();
    }

    private void exportToShapefile() throws Exception {
        SimpleFeatureType schema = CRSLab.featureSource.getSchema();
        JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
        chooser.setDialogTitle("Save reprojected shapefile");
        chooser.setSaveFile(CRSLab.sourceFile);
        int returnVal = chooser.showSaveDialog(null);
        if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file.equals(CRSLab.sourceFile)) {
            JOptionPane.showMessageDialog(null, "Cannot replace " + file);
            return;
        }

        CoordinateReferenceSystem dataCRS = schema.getCoordinateReferenceSystem();
        CoordinateReferenceSystem worldCRS = CRSLab.map.getCoordinateReferenceSystem();
        boolean lenient = true; // allow for some error due to different datums
        MathTransform transform = CRS.findMathTransform(dataCRS, worldCRS, lenient);

        SimpleFeatureCollection featureCollection = CRSLab.featureSource.getFeatures();

        DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
        Map<String, Serializable> create = new HashMap<String, Serializable>();
        create.put("url", file.toURI().toURL());
        create.put("create spatial index", Boolean.TRUE);
        DataStore dataStore = factory.createNewDataStore(create);
        SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(schema, worldCRS);
        dataStore.createSchema(featureType);

        //Get the name of the new Shapefile, which will be used to open the FeatureWriter
        String createdName = dataStore.getTypeNames()[0];

        Transaction transaction = new DefaultTransaction("Reproject");
        FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                dataStore.getFeatureWriterAppend(createdName, transaction);
        SimpleFeatureIterator iterator = featureCollection.features();
        try {
            while (iterator.hasNext()) {
                // copy the contents of each feature and transform the geometry
                SimpleFeature feature = iterator.next();
                SimpleFeature copy = writer.next();
                copy.setAttributes(feature.getAttributes());

                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                Geometry geometry2 = JTS.transform(geometry, transform);

                copy.setDefaultGeometry(geometry2);
                writer.write();
            }
            transaction.commit();
            JOptionPane.showMessageDialog(null, "Export to shapefile complete");
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
            JOptionPane.showMessageDialog(null, "Export to shapefile failed");
        } finally {
            writer.close();
            iterator.close();
            transaction.close();
        }
    }
}
