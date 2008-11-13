/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Arne Kepp / The Open Planning Project 2008 
 */
package org.geowebcache.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.cache.Cache;
import org.geowebcache.layer.GridCalculator;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.mime.MimeType;
import org.geowebcache.util.wms.BBOX;

public class TruncateTask extends GWCTask {
    private static Log log = LogFactory.getLog(org.geowebcache.rest.TruncateTask.class);
    
    private final SeedRequest req;
    
    private final TileLayer tl;
    
    public TruncateTask(SeedRequest req, TileLayer tl) {
        this.req = req;
        this.tl = tl;
    }
    
    void doAction() throws GeoWebCacheException {
        
        tl.isInitialized();
        
        Cache cache = tl.getCache();
        
        BBOX bbox = req.getBounds();
        int[][] bounds = null;
        
        if(bbox != null) {
            bounds = tl.getCoveredGridLevels(req.getSRS(), bbox);
        }
        
        // Check if MimeType supports metatiling, in which case 
        // we may have to throw a wider net
        MimeType mimeType = null;
        if(req.getMimeFormat() != null && req.getMimeFormat().length() > 0) {
            mimeType = MimeType.createFromFormat(req.getMimeFormat());
            
            int[] metaFactors = tl.getMetaTilingFactors();
            
            int gridBounds[][] = tl.getGrid(req.getSRS()).getGridCalculator().getGridBounds();
            
            if(metaFactors[0] > 1 || metaFactors[1] > 1
                    && mimeType.supportsTiling()) {
                bounds = GridCalculator.expandBoundsToMetaTiles(gridBounds, bounds, metaFactors);
            }
        }
        
        cache.truncate(tl, req.getSRS(), 
                req.getZoomStart(), req.getZoomStop(), 
                bounds, mimeType);
    }

}