/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.api;

import com.influir.datacollectionbackend.entities.DetailedMovie;
import com.influir.datacollectionbackend.entities.Movie;
import com.influir.libraries.json.JSONException;
import com.influir.libraries.utils.InfluException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;

/**
 *
 * @author UTAMBE
 */
public class QueryInterface {

    RepositoryConnection conn;
    Repository repository;
    QueryUtility queryUtility;

    public QueryInterface() throws InfluException {
        String endpointURL = "http://fusion.adx.isi.edu:8090/openrdf-sesame/";

        String repositoryName = "csci548Group1";
        /*Connect to endpoint*/
        RepositoryManager repositoryManager = new RemoteRepositoryManager(endpointURL);
        try {
            repositoryManager.initialize();
        } catch (RepositoryException ex) {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            repository = repositoryManager.getRepository(repositoryName);
        } catch (RepositoryConfigException | RepositoryException ex) {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
            throw new InfluException("Unable to get details of movie" + ex.getMessage());
        }
        try {
            conn = repository.getConnection();
        } catch (RepositoryException ex) {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
            throw new InfluException("Unable to get details of movie" + ex.getMessage());
        }
        queryUtility = new QueryUtility(conn, repository);
    }

    DetailedMovie getDetailedMovieDetails(Movie movie) throws InfluException, JSONException {
        DetailedMovie detailedMovie = new DetailedMovie(movie.imdbId, movie.imdbTitle, movie.year, movie.uri);
       
        try {
             /* Get Unique Properties */
            queryUtility.RunQuery(detailedMovie, movie, Influir.QueryType.UNIQUE_PROPERTIES);
            /* Genres */
            queryUtility.RunQuery(detailedMovie, movie, Influir.QueryType.GENRES);
            /* Cast */
            queryUtility.RunQuery(detailedMovie, movie, Influir.QueryType.CAST);
            /* Directors */
            queryUtility.RunQuery(detailedMovie, movie, Influir.QueryType.DIRECTORS);
             /* Similar Movies */
            queryUtility.RunQuery(detailedMovie, movie, Influir.QueryType.SIMILAR_MOVIES_RT);
            /* Keywords */
            queryUtility.RunQuery(detailedMovie, movie, Influir.QueryType.KEYWORDS);
              /* Trailers */
            queryUtility.RunQuery(detailedMovie, movie, Influir.QueryType.TRAILERS);
        } catch (InfluException ex) {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
            throw new InfluException("Unable to get details of movie" + ex.getMessage());
        }

        return detailedMovie;
    }

    Movie getMovieDetails(Movie movie) throws InfluException, JSONException {
       
        try {
             /* Get Abstract Data */
            queryUtility.RunQuery(null, movie, Influir.QueryType.MOVIE_ABSTRACTDATA);
            
        } catch (InfluException ex) {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
            throw new InfluException("Unable to get details of movie" + ex.getMessage());
        }

        return movie;
    }
    
    ArrayList<Movie> getInfluencedByMovies(Movie movie) {
        return null;
    }

    ArrayList<Movie> getInfluencingMovies(Movie movie) {
        return null;
    }

    ArrayList<Movie> getTop250MovieMovies() throws InfluException {
        return queryUtility.RunTop250MovieQuery();
    }
    
     void calculateInfluences(DetailedMovie detailedMovie, ArrayList<DetailedMovie> detailedMovieInfByList, ArrayList<DetailedMovie> detailedMovieInfList) 
     {
        try
        {
            queryUtility.calculateInfluences(detailedMovie, detailedMovieInfByList, detailedMovieInfList);
        }
        catch (QueryEvaluationException ex)
        {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (RepositoryException ex)
        {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (MalformedQueryException ex)
        {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InfluException ex)
        {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (JSONException ex)
        {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
     
}
