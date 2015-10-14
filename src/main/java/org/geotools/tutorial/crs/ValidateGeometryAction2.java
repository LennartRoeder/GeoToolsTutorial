package org.geotools.tutorial.crs;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.swing.JProgressWindow;
import org.geotools.swing.action.SafeAction;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.util.ProgressListener;

import javax.swing.*;
import java.awt.event.ActionEvent;

class ValidateGeometryAction2 extends SafeAction {

    ValidateGeometryAction2() {
        super("Validate geometry");
        putValue(Action.SHORT_DESCRIPTION, "Check each geometry");
    }

    public void action(ActionEvent e) throws Throwable {
        // Here we use the SwingWorker helper class to run the validation routine in a
        // background thread, otherwise the GUI would wait and the progress bar would not be
        // displayed properly
        SwingWorker worker = new SwingWorker<String, Object>() {
            protected String doInBackground() throws Exception {
                // For shapefiles with many features its nice to display a progress bar
                final JProgressWindow progress = new JProgressWindow(null);
                progress.setTitle("Validating feature geometry");

                int numInvalid = validateFeatureGeometry(progress);
                if (numInvalid == 0) {
                    return "All feature geometries are valid";
                } else {
                    return "Invalid geometries: " + numInvalid;
                }
            }
            protected void done() {
                try {
                    Object result = get();
                    JOptionPane.showMessageDialog(null, result, "Geometry results",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ignore) {
                }
            }
        };
        // This statement runs the validation method in a background thread
        worker.execute();
    }

    private int validateFeatureGeometry(ProgressListener progress) throws Exception {
        final SimpleFeatureCollection featureCollection = CRSLab.featureSource.getFeatures();

        // Rather than use an iterator, create a FeatureVisitor to check each fature
        class ValidationVisitor implements FeatureVisitor {
            public int numInvalidGeometries = 0;
            public void visit(Feature f) {
                SimpleFeature feature = (SimpleFeature) f;
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                if (geom != null && !geom.isValid()) {
                    numInvalidGeometries++;
                    System.out.println("Invalid Geoemtry: " + feature.getID());
                }
            }
        }

        ValidationVisitor visitor = new ValidationVisitor();

        // Pass visitor and the progress bar to feature collection
        featureCollection.accepts(visitor, progress);
        return visitor.numInvalidGeometries;
    }
}
