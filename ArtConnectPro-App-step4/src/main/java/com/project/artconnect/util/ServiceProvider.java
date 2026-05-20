package com.project.artconnect.util;

import java.sql.Connection;

import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.persistence.JdbcArtworkDao;
import com.project.artconnect.persistence.JdbcCommunityMemberDao;
import com.project.artconnect.persistence.JdbcGalleryDao;
import com.project.artconnect.persistence.JdbcWorkshopDao;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.service.impl.InMemoryArtistService;
import com.project.artconnect.service.impl.InMemoryArtworkService;
import com.project.artconnect.service.impl.InMemoryCommunityService;
import com.project.artconnect.service.impl.InMemoryGalleryService;
import com.project.artconnect.service.impl.InMemoryWorkshopService;
import com.project.artconnect.service.impl.JdbcArtistService;
import com.project.artconnect.service.impl.JdbcArtworkService;
import com.project.artconnect.service.impl.JdbcCommunityService;
import com.project.artconnect.service.impl.JdbcGalleryService;
import com.project.artconnect.service.impl.JdbcWorkshopService;

public final class ServiceProvider {

    private static final ArtistService    artistService;
    private static final ArtworkService   artworkService;
    private static final GalleryService   galleryService;
    private static final WorkshopService  workshopService;
    private static final CommunityService communityService;

    private static final boolean USE_JDBC;

    static {
        boolean useJdbc = false;
        try (Connection probe = ConnectionManager.getConnection()) {
            useJdbc = probe.isValid(2);
        } catch (Exception ex) {
            System.err.println("[ServiceProvider] Database unreachable, falling back to in-memory services.");
            System.err.println("[ServiceProvider] Reason: " + ex.getMessage());
        }
        USE_JDBC = useJdbc;

        if (USE_JDBC) {
            System.out.println("[ServiceProvider] Using JDBC services against the ArtConnect database.");
            artistService    = new JdbcArtistService(new JdbcArtistDao());
            artworkService   = new JdbcArtworkService(new JdbcArtworkDao());
            galleryService   = new JdbcGalleryService(new JdbcGalleryDao());
            workshopService  = new JdbcWorkshopService(new JdbcWorkshopDao());
            communityService = new JdbcCommunityService(new JdbcCommunityMemberDao());
        } else {
            InMemoryArtistService    a = new InMemoryArtistService();
            InMemoryArtworkService   w = new InMemoryArtworkService();
            InMemoryGalleryService   g = new InMemoryGalleryService();
            InMemoryWorkshopService  ws = new InMemoryWorkshopService();
            InMemoryCommunityService c = new InMemoryCommunityService();
            w.initData(a);
            g.initData(w);
            ws.initData(a);
            c.initData(w);
            artistService    = a;
            artworkService   = w;
            galleryService   = g;
            workshopService  = ws;
            communityService = c;
        }
    }

    private ServiceProvider() { }

    public static boolean isUsingDatabase() { return USE_JDBC; }

    public static ArtistService    getArtistService()    { return artistService;    }
    public static ArtworkService   getArtworkService()   { return artworkService;   }
    public static GalleryService   getGalleryService()   { return galleryService;   }
    public static WorkshopService  getWorkshopService()  { return workshopService;  }
    public static CommunityService getCommunityService() { return communityService; }
}
