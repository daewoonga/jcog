/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jcog.spacegraph.shape.util;

import jcog.spacegraph.gl.Surface;
import jcog.spacegraph.shape.Rect;

/**
 *
 * @author me
 */
public class RectLayout {
    private final Surface surface;

    public RectLayout(Surface surface) {
        this.surface = surface;
    }
    
    public RectLayout withRectInScale(Rect parent, Rect subRect, float dx, float dy, float sx, float sy) {
        subRect.setCenter(new InRect(parent, dx, dy));
        subRect.setScale(new ScaleRect(parent, sx, sy));
        surface.add(subRect);
        return this;
    }
}
