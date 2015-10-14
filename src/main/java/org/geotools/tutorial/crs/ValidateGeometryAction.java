package org.geotools.tutorial.crs;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.swing.action.SafeAction;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.util.ProgressListener;

import javax.swing.*;
import java.awt.event.ActionEvent;

class ValidateGeometryAction extends SafeAction {

    ValidateGeometryAction() {
        super("Validate geometry");
        putValue(Action.SHORT_DESCRIPTION, "Check each geometry");
    }
    public void action(ActionEvent e) throws Throwable {
        int numInvalid = validateFeatureGeometry(null);
        String msg;
        if (numInvalid == 0) {
            msg = "All feature geometries are valid";
        } else {
            msg = "Invalid geometries: " + numInvalid;
        }
        JOptionPane.showMessageDialog(null, msg, "Geometry results",
                JOptionPane.INFORMATION_MESSAGE);
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