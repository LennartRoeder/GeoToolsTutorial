package org.geotools.tutorial.crs;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;

import javax.swing.*;
import java.io.File;

/**
 * This is a visual example of changing the coordinate reference system of a feature layer.
 */
public class CRSLab {

    static File sourceFile;
    static SimpleFeatureSource featureSource;
    static MapContent map;

    public static void main(String[] args) throws Exception {
        displayShapefile();
    }

    private static void displayShapefile() throws Exception {
        sourceFile = new File("res/crs/bc_border.shp");
        FileDataStore store = FileDataStoreFinder.getDataStore(sourceFile);
        featureSource = store.getFeatureSource();

        // Create a map context and add our shapefile to it
        map = new MapContent();
        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.layers().add(layer);

        // Create a JMapFrame with custom toolbar buttons
        JMapFrame mapFrame = new JMapFrame(map);
        mapFrame.enableToolBar(true);
        mapFrame.enableStatusBar(true);

        JToolBar toolbar = mapFrame.getToolBar();
        toolbar.addSeparator();
//        toolbar.add(new JButton(new ValidateGeometryAction()));
        toolbar.add(new JButton(new ValidateGeometryAction2()));
        toolbar.add(new JButton(new ExportShapefileAction()));

        // Display the map frame. When it is closed the application will exit
        mapFrame.setSize(800, 600);
        mapFrame.setVisible(true);
    }
}