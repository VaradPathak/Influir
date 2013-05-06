package com.influir.api;

import com.influir.api.Influir.QueryType;
import static com.influir.api.Influir.QueryType.CAST;
import static com.influir.api.Influir.QueryType.DIRECTORS;
import static com.influir.api.Influir.QueryType.DIRECTOR_INFLUENCEDBY;
import static com.influir.api.Influir.QueryType.GENRES;
import static com.influir.api.Influir.QueryType.INFLUENCEDBY_DIRECTOR;
import static com.influir.api.Influir.QueryType.KEYWORDS;
import static com.influir.api.Influir.QueryType.MOVIE_ABSTRACTDATA;
import static com.influir.api.Influir.QueryType.TRAILERS;
import com.influir.datacollectionbackend.entities.DetailedMovie;
import com.influir.datacollectionbackend.entities.InfluirScore;
import com.influir.datacollectionbackend.entities.Movie;
import com.influir.datacollectionbackend.entities.Person;
import com.influir.libraries.json.JSONException;
import com.influir.libraries.json.JSONObject;
import com.influir.libraries.utils.Constants;
import com.influir.libraries.utils.InfluException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/**
 *
 * @author UTAMBE
 */
public class QueryUtility
{
    RepositoryConnection conn;
    Repository repository;
    private Influir.QueryType DIRECTOR_INFLUENCEDBY;
    private Influir.QueryType INFLU_DATA;

    public QueryUtility(RepositoryConnection connection, Repository repository)
    {
        this.conn = connection;
        this.repository = repository;
    }

    private QueryUtility()
    {
    }

    private String GetQuery(String indentifier, Influir.QueryType operation)
    {
        String sparqlQuery = "";
        switch (operation)
        {
            /*Unique Properties*/
            case UNIQUE_PROPERTIES:
                sparqlQuery = "PREFIX influir:<http://influir.com/>\n"
                        + "                         PREFIX dcterms:<http://purl.org/dc/terms/> \n"
                        + "                         PREFIX foaf:<http://xmlns.com/foaf/0.1/> \n"
                        + "                         PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>\n"
                        + "                         PREFIX dbp-ont:<http://dbpedia.org/ontology/>\n"
                        + "                         PREFIX ontology:<http://dbpedia.org/ontology/>\n"
                        + "                         SELECT ?releasedDate ?rtTitle ?audienceRating ?criticRating ?revenue ?studio ?posterURL ?httpURL ?rtid ?tmdbid ?imdbid \n"
                        + "                         WHERE\n"
                        + "                         {\n"
                        + "                         OPTIONAL{" + indentifier + " movieontology:releasedate ?releasedDate}.\n"
                        + "                         OPTIONAL{" + indentifier + " movieontology:title ?rtTitle}.\n"
                        + "                         OPTIONAL{" + indentifier + " movieontology:imdbrating ?audienceRating}.\n"
                        + "                         OPTIONAL{" + indentifier + " movieontology:criticsrating ?criticRating}.\n"
                        + "                         OPTIONAL{" + indentifier + " dbp-ont:gross ?revenue}.\n"
                        + "                         OPTIONAL{" + indentifier + " movieontology:tmdbid ?tmdbid}.\n"
                        + "                         OPTIONAL{" + indentifier + " dcterms:identifier ?imdbid}.\n"
                        + "                         OPTIONAL{" + indentifier + " movieontology:rtid ?rtid}.\n"
                        + "                         OPTIONAL{" + indentifier + " movieontology:isFromStudio ?moviestudio.\n"
                        + "                         ?moviestudio foaf:title ?studio}.\n"
                        + "                         OPTIONAL{" + indentifier + " ontology:creationYear ?year}.\n"
                        + "                         OPTIONAL{" + indentifier + " movieontology:hasPoster ?poster.\n"
                        + "                         ?poster movieontology:url ?posterURL}.\n"
                        + "                         OPTIONAL{" + indentifier + " movieontology:url ?httpURL}\n"
                        + "                         }\n"
                        + "                         LIMIT 1";
                break;
            /* Genre */
            case GENRES:
                sparqlQuery = "PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>"
                        + "PREFIX foaf:<http://xmlns.com/foaf/0.1/> "
                        + "SELECT ?tmdbGenres "
                        + "WHERE"
                        + "{"
                        + indentifier + " movieontology:belongsToGenre ?genre."
                        + "?genre foaf:name ?tmdbGenres"
                        + "}";
                break;
            /* Cast */
            case CAST:
                sparqlQuery = "PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>\n"
                        + "PREFIX dcterms:<http://purl.org/dc/terms/>\n"
                        + "PREFIX foaf:<http://xmlns.com/foaf/0.1/>\n"
                        + "SELECT ?cast ?castId\n"
                        + "WHERE\n"
                        + "{\n"
                        + indentifier + " movieontology:hasActor ?actor.\n"
                        + "?actor foaf:name ?cast.\n"
                        + "?actor dcterms:identifier ?castId\n"
                        + "}";
                break;
            /* Directors */
            case DIRECTORS:
                sparqlQuery = "PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>\n"
                        + "PREFIX dc:<http://purl.org/dc/elements/1.1/>\n"
                        + "PREFIX foaf:<http://xmlns.com/foaf/0.1/>\n"
                        + "SELECT ?director ?directorId\n"
                        + "WHERE\n"
                        + "{\n"
                        + indentifier + " movieontology:hasDirector ?movieDirector.\n"
                        + "?movieDirector foaf:name ?director.\n"
                        + "?movieDirector dc:identifier ?directorId\n"
                        + "}";
                break;
            /* Similar Movies RT */
            case SIMILAR_MOVIES_RT:
                sparqlQuery = "PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>\n"
                        + "SELECT ?similarMovie\n"
                        + "WHERE\n"
                        + "{\n"
                        + indentifier + " movieontology:hasSimilarMovie ?similarMovie."
                        //+ "?simMovie movieontology:rtid ?similarMovie\n"
                        + "}";
                break;
            /* Keywords */
            case KEYWORDS:
                sparqlQuery = "PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>\n"
                        + "SELECT ?keyword\n"
                        + "WHERE\n"
                        + "{\n"
                        + indentifier + " movieontology:keyword ?keyword\n"
                        + "}";
                break;
            /* Top 250 Movies */
            case TOP250:
                sparqlQuery = "PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>\n"
                        + "PREFIX ontology:<http://dbpedia.org/ontology/>\n"
                        + "SELECT ?title ?year ?rtId ?rating ?posterURL ?movie \n"
                        + "WHERE\n"
                        + "{\n"
                        + "?movie movieontology:imdbrating ?rating.\n"
                        + "?movie ontology:creationYear ?year.\n"
                        + "?movie movieontology:rttitle ?title.\n"
                        + "?movie movieontology:rtid ?rtId\n"
                        + "OPTIONAL{?movie movieontology:hasPoster ?poster.\n"
                        + "?poster movieontology:url ?posterURL}.\n"
                        + "} ORDER BY DESC(?rating) LIMIT 250";
                break;
            /* Abstract Data */
            case MOVIE_ABSTRACTDATA:
                sparqlQuery = "PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>\n"
                        + "PREFIX ontology:<http://dbpedia.org/ontology/>\n"
                        + "SELECT ?title ?year \n"
                        + "WHERE\n"
                        + "{\n"
                        + indentifier + " movieontology:rttitle ?title.\n"
                        + indentifier + " ontology:creationYear ?year\n"
                        + "}";
                break;
            /* Trailers */
            case TRAILERS:
                sparqlQuery = "PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>\n"
                        + " PREFIX foaf:<http://xmlns.com/foaf/0.1/>\n"
                        + " PREFIX dc:<http://purl.org/dc/elements/1.1/>\n"
                        + " SELECT  ?trailerId ?trailer\n"
                        + " WHERE\n"
                        + " {\n"
                        + indentifier + " movieontology:hasTrailer ?mvtrailer.\n"
                        + " ?mvtrailer foaf:name ?trailer.\n"
                        + " ?mvtrailer dc:identifier ?trailerId\n"
                        + "  }\n";
                break;
            /* Influenced By Director */
            case INFLUENCEDBY_DIRECTOR:
                sparqlQuery = "PREFIX ontology:<http://dbpedia.org/ontology/> PREFIX foaf:<http://xmlns.com/foaf/0.1/>\n"
                        + "SELECT ?director1 WHERE\n"
                        + "{?director foaf:name \"" + indentifier + "\".?director2 ontology:influencedBy ?director. ?director2 foaf:name ?director1}";
                break;
            /* Director Influenced By */
            case DIRECTOR_INFLUENCEDBY:
                sparqlQuery = "PREFIX ontology:<http://dbpedia.org/ontology/> PREFIX foaf:<http://xmlns.com/foaf/0.1/>\n"
                        + "SELECT ?director1 WHERE\n"
                        + "{?director foaf:name \"" + indentifier + "\".?director ontology:influencedBy ?director2. ?director2 foaf:name ?director1}";
                break;
            /* Influence Related Data */
            case INFLU_DATA:
                sparqlQuery = "PREFIX influir:<http://influir.com/>\n"
                        + "PREFIX dcterms:<http://purl.org/dc/terms/> \n"
                        + "PREFIX foaf:<http://xmlns.com/foaf/0.1/> \n"
                        + "PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>\n"
                        + "PREFIX dbp-ont:<http://dbpedia.org/ontology/>\n"
                        + "PREFIX ontology:<http://dbpedia.org/ontology/>\n"
                        + "SELECT ?rtTitle ?audienceRating ?posterURL ?httpURL ?movie\n"
                        + "WHERE\n"
                        + "{\n"
                        + "?director foaf:name \"" + indentifier + "\".\n"
                        + "?movie movieontology:hasDirector ?director.\n"
                        + "OPTIONAL{  ?movie   movieontology:title ?rtTitle}.\n"
                        + "OPTIONAL{  ?movie   movieontology:imdbrating ?audienceRating}.\n"
                        + "OPTIONAL{  ?movie   movieontology:hasPoster ?poster.\n"
                        + "?poster movieontology:url ?posterURL}.\n"
                        + "OPTIONAL{  ?movie   movieontology:url ?httpURL}\n"
                        + "}\n"
                        + "LIMIT 1";
                break;

        }
        return sparqlQuery;
    }

    ArrayList<Movie> RunTop250MovieQuery() throws InfluException
    {
        String sparqlQuery = GetQuery("", Influir.QueryType.TOP250);
        TupleQuery query = null;
        ArrayList<Movie> top250 = new ArrayList<>();

        try
        {
            query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        }
        catch (RepositoryException | MalformedQueryException ex)
        {
            Logger.getLogger(QueryUtility.class.getName()).log(Level.SEVERE, null, ex);
            throw new InfluException("Unable to get details of movie" + ex.getMessage());
        }
        TupleQueryResult result = null;
        try
        {
            result = query.evaluate();
        }
        catch (QueryEvaluationException ex)
        {
            Logger.getLogger(QueryUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        try
        {
            while (result.hasNext())
            {
                DetailedMovie movie = new DetailedMovie();
                BindingSet bindingSet = result.next();
                for (String bindingName : bindingSet.getBindingNames())
                {
                    switch (bindingName)
                    {
                        case "title":
                            movie.imdbTitle = bindingSet.getValue(bindingName).stringValue();
                            break;
                        case "year":
                            try
                            {
                                movie.year = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                            }
                            catch (NullPointerException ex)
                            {
                                movie.year = -1;
                            }
                            break;
                        case "rtId":
                            try
                            {
                                movie.rtId = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                            }
                            catch (NullPointerException ex)
                            {
                                movie.rtId = -1;
                            }
                            break;
                        case "rating":
                            try
                            {
                                movie.audienceRating = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                            }
                            catch (NullPointerException ex)
                            {
                                movie.audienceRating = -1;
                            }
                            break;
                        case "posterURL":
                            movie.posterURL = bindingSet.getValue(bindingName).stringValue();
                            break;
                        case "movie":
                            movie.uri = "<" + bindingSet.getValue(bindingName).stringValue() + ">";
                            break;
                    }
                }
                top250.add(movie);
            }
        }
        catch (QueryEvaluationException ex)
        {
            Logger.getLogger(QueryUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return top250;
    }

    void RunQuery(DetailedMovie detailedMovie, Movie movie, Influir.QueryType operation) throws InfluException, JSONException
    {
        try
        {
            String sparqlQuery;
            sparqlQuery = GetQuery(movie.uri, operation);

            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
            TupleQueryResult result = query.evaluate();

            String name = "";
            Integer id = 0;
            String strId = "";
            switch (operation)
            {
                case GENRES:
                    detailedMovie.tmdbGenres = new ArrayList<>();
                    break;
                case CAST:
                    detailedMovie.cast = new ArrayList<>();
                    break;
                case DIRECTORS:
                    detailedMovie.directors = new ArrayList<>();
                    break;
                case SIMILAR_MOVIES_RT:
                    detailedMovie.rtSimilarMovies = new ArrayList<>();
                    break;
                case KEYWORDS:
                    detailedMovie.keywords = new ArrayList<>();
                    break;
                case TRAILERS:
                    detailedMovie.trailers = new ArrayList<>();
                    break;
            }
            while (result.hasNext())
            {

                BindingSet bindingSet = result.next();
                for (String bindingName : bindingSet.getBindingNames())
                {
                    switch (operation)
                    {
                        /* Abstract Data */
                        case MOVIE_ABSTRACTDATA:
                        {
                            switch (bindingName)
                            {
                                case "title":
                                    movie.imdbTitle = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "year":
                                    try
                                    {
                                        movie.year = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        movie.year = -1;
                                    }
                                    break;
                                case "rtId":
                                    try
                                    {
                                        movie.imdbId = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        movie.imdbId = -1;
                                    }
                                    break;
                            }
                        }
                        break;
                        /*Unique Properties*/
                        case UNIQUE_PROPERTIES:
                        {
                            switch (bindingName)
                            {
                                case "rtTitle":
                                    detailedMovie.rtTitle = bindingSet.getValue(bindingName).stringValue();
                                    detailedMovie.imdbTitle = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "releasedDate":
                                    detailedMovie.releasedDate = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "audienceRating":
                                    try
                                    {
                                        detailedMovie.audienceRating = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        detailedMovie.audienceRating = -1;
                                    }
                                    break;
                                case "criticRating":
                                    try
                                    {
                                        detailedMovie.criticRating = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        detailedMovie.criticRating = -1;
                                    }
                                    break;
                                case "year":
                                    try
                                    {
                                        detailedMovie.year = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        detailedMovie.year = -1;
                                    }
                                    break;
                                case "revenue":
                                    try
                                    {
                                        detailedMovie.revenue = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        detailedMovie.revenue = -1;
                                    }
                                    break;
                                case "studio":
                                    detailedMovie.studio = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "criticsConsensus":
                                    detailedMovie.criticsConsensus = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "httpURL":
                                    detailedMovie.httpURL = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "posterURL":
                                    detailedMovie.posterURL = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "imdbId":
                                    try
                                    {
                                        detailedMovie.imdbId = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        detailedMovie.imdbId = -1;
                                    }
                                    break;
                                case "rtId":
                                    try
                                    {
                                        detailedMovie.rtId = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        detailedMovie.rtId = -1;
                                    }
                                    break;
                                case "tmdbId":
                                    try
                                    {
                                        detailedMovie.tmdbId = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        detailedMovie.tmdbId = -1;
                                    }
                                    break;

                            }
                            break;
                        }
                        /* Genre */
                        case GENRES:
                        {
                            switch (bindingName)
                            {
                                case "tmdbGenres":
                                    detailedMovie.tmdbGenres.add(bindingSet.getValue(bindingName).stringValue());
                                    break;
                            }
                            break;
                        }
                        /* Cast */
                        case CAST:
                        {
                            switch (bindingName)
                            {
                                case "cast":
                                    name = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "castId":
                                    try
                                    {
                                        id = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        id = -1;
                                    }
                                    break;
                            }
                            break;
                        }
                        /* Directors */
                        case DIRECTORS:
                        {
                            switch (bindingName)
                            {
                                case "director":
                                    name = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "directorId":
                                    try
                                    {
                                        id = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        id = -1;
                                    }
                                    break;
                            }
                            break;
                        }
                        /* Similar Movies RT */
                        case SIMILAR_MOVIES_RT:
                        {
                            switch (bindingName)
                            {
                                case "similarMovie":
                                    detailedMovie.rtSimilarMovies.add(bindingSet.getValue(bindingName).stringValue());
                                    break;
                            }
                            break;
                        }
                        /* Keywords */
                        case KEYWORDS:
                        {
                            switch (bindingName)
                            {
                                case "keyword":
                                    detailedMovie.keywords.add(bindingSet.getValue(bindingName).stringValue());
                                    break;
                            }
                            break;
                        }
                        /* TRAILERS */
                        case TRAILERS:
                        {
                            switch (bindingName)
                            {
                                case "trailer":
                                    name = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "trailerId":
                                    strId = bindingSet.getValue(bindingName).stringValue();
                                    break;
                            }
                        }
                        break;
                        /* INF MOVIE DATA */
                        case INFLU_DATA:
                        {
                            switch (bindingName)
                            {
                                case "rtTitle":
                                    detailedMovie.rtTitle = bindingSet.getValue(bindingName).stringValue();
                                    detailedMovie.imdbTitle = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "audienceRating":
                                    try
                                    {
                                        detailedMovie.audienceRating = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        detailedMovie.audienceRating = -1;
                                    }
                                    break;
                                case "httpURL":
                                    detailedMovie.httpURL = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "posterURL":
                                    detailedMovie.posterURL = bindingSet.getValue(bindingName).stringValue();
                                    break;
                                case "year":
                                    try
                                    {
                                        detailedMovie.year = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                    }
                                    catch (NullPointerException ex)
                                    {
                                        detailedMovie.year = -1;
                                    }
                                    break;
                                case "movie":
                                    detailedMovie.uri = "<" + bindingSet.getValue(bindingName).stringValue() + ">";
                                    break;

                            }
                        }
                        break;
                    }

                }
                switch (operation)
                {
                    case CAST:
                        detailedMovie.cast.add(new Person(name, id));
                        break;
                    case DIRECTORS:
                        detailedMovie.directors.add(new Person(name, id));
                        break;
                    case TRAILERS:
                        JSONObject trailer = new JSONObject();
                        trailer.put(Constants.TRAILERTYPE, name);
                        trailer.put(Constants.TRAILERID, strId);
                        detailedMovie.trailers.add(trailer);
                        break;
                }
            }
        }
        catch (MalformedQueryException | QueryEvaluationException e)
        {
            throw new InfluException("Unable to get details of movie" + e.getMessage());
        }
        catch (RepositoryException ex)
        {
            Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE, null, ex);
            throw new InfluException("Unable to get details of movie" + ex.getMessage());
        }
        finally
        {
            try
            {
                conn.close();
            }
            catch (RepositoryException e)
            {
                throw new InfluException("Unable to get details of movie" + e.getMessage());
            }
        }
    }

    public void calculateInfluences(DetailedMovie detailedMovie, ArrayList<DetailedMovie> detailedMovieInfByList, ArrayList<DetailedMovie> detailedMovieInfList) throws QueryEvaluationException, RepositoryException, MalformedQueryException, InfluException, JSONException
    {
        //Get All Directors Inf and Inf by
        String sparqlQuery = "";
        int infMaxA = 0;
        int infMaxB = 0;
        Influir.QueryType operation = QueryType.DIRECTOR_INFLUENCEDBY;
        ArrayList<String> director_InfluencedBy = new ArrayList<>();
        HashMap<String, String> dirMap = new HashMap<>();
        for (int i = 0; i < detailedMovie.directors.size(); i++)
        {
            sparqlQuery = GetQuery(detailedMovie.directors.get(i).name, operation);
            getInfDirectors(operation, sparqlQuery, director_InfluencedBy, dirMap, detailedMovie.directors.get(i).name);
        }
        Influir.QueryType operation2 = QueryType.INFLUENCEDBY_DIRECTOR;
        ArrayList<String> influencedBy_Director = new ArrayList<>();
        for (int i = 0; i < detailedMovie.directors.size(); i++)
        {
            sparqlQuery = GetQuery(detailedMovie.directors.get(i).name, operation2);
            getInfDirectors(operation2, sparqlQuery, influencedBy_Director, dirMap, detailedMovie.directors.get(i).name);
        }


        HashMap<String, Influence> Inf_Lvl2a = new HashMap<>();
        for (String director : director_InfluencedBy)
        {
            Influence inf = new Influence();
            inf.director_InfluencedBy_Lvl2 = new HashMap<>();
            ArrayList<String> directors = new ArrayList<>();
            sparqlQuery = GetQuery(director, operation);
            getInfDirectors(operation, sparqlQuery, directors, null, null);
            inf.director_InfluencedBy_Lvl2.put(director, directors);

            inf.influencedBy_Director_Lvl2 = new HashMap<>();
            ArrayList<String> directors1 = new ArrayList<>();
            sparqlQuery = GetQuery(director, operation2);
            getInfDirectors(operation2, sparqlQuery, directors1, null, null);
            inf.influencedBy_Director_Lvl2.put(director, directors1);

            int influencedByScore = inf.director_InfluencedBy_Lvl2.get(director).size();
            int influencesScore = inf.influencedBy_Director_Lvl2.get(director).size();
            inf.score = influencesScore / (influencedByScore == 0 ? 1 : influencedByScore);
            if (infMaxA < inf.score)
            {
                infMaxA = inf.score;
            }
            Inf_Lvl2a.put(director, inf);
        }

        HashMap<String, Influence> Inf_Lvl2b = new HashMap<>();
        for (String director : influencedBy_Director)
        {
            Influence inf = new Influence();
            inf.director_InfluencedBy_Lvl2 = new HashMap<>();
            ArrayList<String> directors = new ArrayList<>();
            sparqlQuery = GetQuery(director, operation);
            getInfDirectors(operation, sparqlQuery, directors, null, null);
            inf.director_InfluencedBy_Lvl2.put(director, directors);

            inf.influencedBy_Director_Lvl2 = new HashMap<>();
            ArrayList<String> directors1 = new ArrayList<>();
            sparqlQuery = GetQuery(director, operation2);
            getInfDirectors(operation, sparqlQuery, directors, null, null);
            inf.influencedBy_Director_Lvl2.put(director, directors);

            int influencedByScore = inf.director_InfluencedBy_Lvl2.get(director).size();
            int influencesScore = inf.influencedBy_Director_Lvl2.get(director).size();
            inf.score = influencesScore / (influencedByScore == 0 ? 1 : influencedByScore);
            if (infMaxB < inf.score)
            {
                infMaxB = inf.score;
            }
            Inf_Lvl2b.put(director, inf);
        }
        String filter = "";
        TupleQuery query;
        TupleQueryResult result;
        if (director_InfluencedBy.size() > 0)
        {
            for (int i = 0; i < director_InfluencedBy.size(); i++)
            {
                if (i != 0)
                {
                    filter = filter + "|| ?dir = \"" + director_InfluencedBy.get(i) + "\"";
                }
                else
                {
                    filter = filter + " ?dir = \"" + director_InfluencedBy.get(i) + "\"";
                }
            }

            sparqlQuery = "PREFIX influir:<http://influir.com/>\n"
                    + "PREFIX dcterms:<http://purl.org/dc/terms/> \n"
                    + "PREFIX foaf:<http://xmlns.com/foaf/0.1/> \n"
                    + "PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>\n"
                    + "PREFIX dbp-ont:<http://dbpedia.org/ontology/>\n"
                    + "PREFIX ontology:<http://dbpedia.org/ontology/>\n"
                    + "SELECT ?rtTitle ?audienceRating ?posterURL ?httpURL ?movie ?criticRating ?year ?dir\n"
                    + "WHERE\n"
                    + "{\n"
                    + "?director foaf:name ?dir.\n"
                    + "?movie movieontology:hasDirector ?director.\n"
                    + "OPTIONAL{  ?movie   movieontology:title ?rtTitle}.\n"
                    + "OPTIONAL{  ?movie   movieontology:imdbrating ?audienceRating}.\n"
                    + "OPTIONAL{  ?movie   movieontology:criticsrating ?criticRating}."
                    + "OPTIONAL{  ?movie   movieontology:hasPoster ?poster.\n"
                    + "?poster movieontology:url ?posterURL}.\n"
                    + "OPTIONAL{  ?movie   ontology:creationYear ?year}.\n"
                    + "OPTIONAL{  ?movie   movieontology:url ?httpURL}"
                    + ".FILTER(" + filter + ")\n"
                    + "}";

            query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
            result = query.evaluate();
            //detailedMovieInfByList = new ArrayList<>();

            while (result.hasNext())
            {

                BindingSet bindingSet = result.next();
                DetailedMovie detailedMovie1 = new DetailedMovie();
                detailedMovie1.influirScore = new InfluirScore();
                for (String bindingName : bindingSet.getBindingNames())
                {
                    switch (bindingName)
                    {
                        case "rtTitle":
                            detailedMovie1.rtTitle = bindingSet.getValue(bindingName).stringValue();
                            detailedMovie1.imdbTitle = bindingSet.getValue(bindingName).stringValue();
                            break;
                        case "audienceRating":
                            try
                            {
                                detailedMovie1.audienceRating = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                detailedMovie1.influirScore.audienceScore = detailedMovie1.audienceRating;
                            }
                            catch (NullPointerException ex)
                            {
                                detailedMovie1.audienceRating = -1;
                            }
                            break;
                        case "httpURL":
                            detailedMovie1.httpURL = bindingSet.getValue(bindingName).stringValue();
                            break;
                        case "dir":
                            if (Inf_Lvl2a.containsKey((bindingSet.getValue(bindingName).stringValue())) && detailedMovie1.influirScore != null)
                            {
                                detailedMovie1.influirScore.influScore = Inf_Lvl2a.get((bindingSet.getValue(bindingName).stringValue())).score;
                                detailedMovie1.influirScore.movieDirector = dirMap.get((bindingSet.getValue(bindingName).stringValue()));
                            }
                            else
                            {
                                if (detailedMovie1.influirScore == null)
                                {
                                    detailedMovie1.influirScore = new InfluirScore();
                                }
                                detailedMovie1.influirScore.influScore = 0;
                            }
                            detailedMovie1.influirScore.influencedByDirector = bindingSet.getValue(bindingName).stringValue();
                            break;
                        case "posterURL":
                            detailedMovie1.posterURL = bindingSet.getValue(bindingName).stringValue();
                            break;
                        case "year":
                            try
                            {
                                detailedMovie1.year = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                            }
                            catch (NullPointerException ex)
                            {
                                detailedMovie1.year = -1;
                            }
                            break;
                        case "movie":
                            detailedMovie1.uri = "<" + bindingSet.getValue(bindingName).stringValue() + ">";
                            break;
                        case "criticRating":
                            try
                            {
                                detailedMovie1.criticRating = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                detailedMovie1.influirScore.criticScore = detailedMovie1.criticRating;
                            }
                            catch (NullPointerException ex)
                            {
                                detailedMovie1.criticRating = -1;
                            }
                            break;

                    }
                }
                /* Genres */
                Movie movie = new Movie(detailedMovie1.imdbId, detailedMovie1.imdbTitle, detailedMovie1.year, detailedMovie1.uri);
                RunQuery(detailedMovie1, movie, Influir.QueryType.GENRES);
                /* Keywords */
                RunQuery(detailedMovie1, movie, Influir.QueryType.KEYWORDS);
                if (detailedMovie1.imdbTitle != null && !detailedMovie1.imdbTitle.equals("Dr. Strangelove or: How I Learned to Stop Worrying and Love the Bomb"))
                {
                    detailedMovieInfByList.add(detailedMovie1);
                }
            }

            for (DetailedMovie dm : detailedMovieInfByList)
            {
                dm.influirScore.genreScore = 0;
                if (detailedMovie.rtGenres != null)
                {
                    for (String genre : dm.rtGenres)
                    {
                        if (detailedMovie.rtGenres != null && detailedMovie.rtGenres.contains(genre))
                        {
                            dm.influirScore.genreScore++;
                        }
                    }
                }

                dm.influirScore.keywordScore = 0;
                if (detailedMovie.keywords != null)
                {
                    for (String genre : dm.keywords)
                    {
                        if (detailedMovie.keywords != null && detailedMovie.keywords.contains(genre))
                        {
                            dm.influirScore.keywordScore++;
                        }
                    }
                }
                if (dm.influirScore != null)
                {
                    int gSize = 0, kSize = 0;
                    if (detailedMovie.rtGenres != null)
                    {
                        gSize = detailedMovie.rtGenres.isEmpty() ? 1 : detailedMovie.rtGenres.size();
                    }
                    else
                    {
                        gSize = 1;
                    }
                    if (dm.influirScore != null)
                    {
                        if (detailedMovie.keywords != null)
                        {
                            kSize = detailedMovie.keywords.isEmpty() ? 1 : detailedMovie.keywords.size();
                        }
                        else
                        {
                            kSize = 1;
                        }
                    }
                    dm.influirScore.movieScore = (float) ((0.4) * dm.influirScore.influScore / (infMaxA == 0 ? 1 : infMaxA)) + (float) ((0.15) * (dm.influirScore.genreScore * 100 / gSize)) + (float) ((0.05) * (dm.influirScore.keywordScore * 100 / kSize)) + (float) ((0.2) * dm.influirScore.criticScore) + (float) ((0.2) * dm.influirScore.audienceScore);
                    int score = (int) (dm.influirScore.movieScore * 100);
                    dm.influirScore.movieScore = ((float) score) / 100;
                }
            }

        }
        //Get Movie Details for those directors


        if (influencedBy_Director.size() > 0)
        {
            filter = "";
            for (int i = 0; i < influencedBy_Director.size(); i++)
            {
                if (i != 0)
                {
                    filter = filter + "|| ?dir = \"" + influencedBy_Director.get(i) + "\"";
                }
                else
                {
                    filter = filter + " ?dir = \"" + influencedBy_Director.get(i) + "\"";
                }
            }
            sparqlQuery = "PREFIX influir:<http://influir.com/>\n"
                    + "PREFIX dcterms:<http://purl.org/dc/terms/> \n"
                    + "PREFIX foaf:<http://xmlns.com/foaf/0.1/> \n"
                    + "PREFIX movieontology:<http://www.movieontology.org/2009/10/01/movieontology.owl#>\n"
                    + "PREFIX dbp-ont:<http://dbpedia.org/ontology/>\n"
                    + "PREFIX ontology:<http://dbpedia.org/ontology/>\n"
                    + "SELECT ?rtTitle ?audienceRating ?posterURL ?httpURL ?movie ?criticRating ?year ?dir\n"
                    + "WHERE\n"
                    + "{\n"
                    + "?director foaf:name ?dir.\n"
                    + "?movie movieontology:hasDirector ?director.\n"
                    + "OPTIONAL{  ?movie   movieontology:title ?rtTitle}.\n"
                    + "OPTIONAL{  ?movie   movieontology:imdbrating ?audienceRating}.\n"
                    + "OPTIONAL{  ?movie   movieontology:criticsrating ?criticRating}."
                    + "OPTIONAL{  ?movie   movieontology:hasPoster ?poster.\n"
                    + "?poster movieontology:url ?posterURL}.\n"
                    + "OPTIONAL{  ?movie   ontology:creationYear ?year}.\n"
                    + "OPTIONAL{  ?movie   movieontology:url ?httpURL}"
                    + ".FILTER(" + filter + ")\n"
                    + "}";

            query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
            result = query.evaluate();
            //detailedMovieInfList = new ArrayList<>();

            while (result.hasNext())
            {

                BindingSet bindingSet = result.next();
                DetailedMovie detailedMovie1 = new DetailedMovie();
                detailedMovie1.influirScore = new InfluirScore();
                for (String bindingName : bindingSet.getBindingNames())
                {
                    switch (bindingName)
                    {
                        case "rtTitle":
                            detailedMovie1.rtTitle = bindingSet.getValue(bindingName).stringValue();
                            detailedMovie1.imdbTitle = bindingSet.getValue(bindingName).stringValue();
                            break;
                        case "audienceRating":
                            try
                            {
                                detailedMovie1.audienceRating = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                detailedMovie1.influirScore.audienceScore = detailedMovie1.audienceRating;
                            }
                            catch (NullPointerException ex)
                            {
                                detailedMovie1.audienceRating = -1;
                            }
                            break;
                        case "httpURL":
                            detailedMovie1.httpURL = bindingSet.getValue(bindingName).stringValue();
                            break;
                        case "dir":
                            if (Inf_Lvl2b.containsKey((bindingSet.getValue(bindingName).stringValue())))
                            {
                                detailedMovie1.influirScore.influScore = Inf_Lvl2b.get(bindingSet.getValue(bindingName).stringValue()).score;
                                detailedMovie1.influirScore.movieDirector = dirMap.get((bindingSet.getValue(bindingName).stringValue()));
                            }
                            else
                            {
                                if (detailedMovie1.influirScore == null)
                                {
                                    detailedMovie1.influirScore = new InfluirScore();
                                }
                                detailedMovie1.influirScore.influScore = 0;
                            }
                            detailedMovie1.influirScore.influencingDirector = bindingSet.getValue(bindingName).stringValue();
                            break;
                        case "posterURL":
                            detailedMovie1.posterURL = bindingSet.getValue(bindingName).stringValue();
                            break;
                        case "year":
                            try
                            {
                                detailedMovie1.year = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                            }
                            catch (NullPointerException ex)
                            {
                                detailedMovie1.year = -1;
                            }
                            break;
                        case "movie":
                            detailedMovie1.uri = "<" + bindingSet.getValue(bindingName).stringValue() + ">";
                            break;
                        case "criticRating":
                            try
                            {
                                detailedMovie1.criticRating = Integer.parseInt(bindingSet.getValue(bindingName).stringValue());
                                detailedMovie1.influirScore.criticScore = detailedMovie1.criticRating;
                            }
                            catch (NullPointerException ex)
                            {
                                detailedMovie1.criticRating = -1;
                            }
                            break;

                    }

                }
                /* Genres */
                Movie movie = new Movie(detailedMovie1.imdbId, detailedMovie1.imdbTitle, detailedMovie1.year, detailedMovie1.uri);
                RunQuery(detailedMovie1, movie, Influir.QueryType.GENRES);
                /* Keywords */
                RunQuery(detailedMovie1, movie, Influir.QueryType.KEYWORDS);
                if (detailedMovie1.imdbTitle != null && !detailedMovie1.imdbTitle.equals("Dr. Strangelove or: How I Learned to Stop Worrying and Love the Bomb"))
                {
                    detailedMovieInfList.add(detailedMovie1);
                }
            }
            for (DetailedMovie dm : detailedMovieInfList)
            {
                dm.influirScore.genreScore = 0;
                if (detailedMovie.rtGenres != null)
                {
                    for (String genre : dm.rtGenres)
                    {
                        if (detailedMovie.rtGenres != null && detailedMovie.rtGenres.contains(genre))
                        {
                            dm.influirScore.genreScore++;
                        }
                    }
                }

                dm.influirScore.keywordScore = 0;
                if (detailedMovie.keywords != null)
                {
                    for (String genre : dm.keywords)
                    {
                        if (detailedMovie.keywords != null && detailedMovie.keywords.contains(genre))
                        {
                            dm.influirScore.keywordScore++;
                        }
                    }
                }
                if (dm.influirScore != null)
                {
                    int gSize = 0, kSize = 0;
                    if (detailedMovie.rtGenres != null)
                    {
                        gSize = detailedMovie.rtGenres.isEmpty() ? 1 : detailedMovie.rtGenres.size();
                    }
                    else
                    {
                        gSize = 1;
                    }
                    if (dm.influirScore != null)
                    {
                        if (detailedMovie.keywords != null)
                        {
                            kSize = detailedMovie.keywords.isEmpty() ? 1 : detailedMovie.keywords.size();
                        }
                        else
                        {
                            kSize = 1;
                        }
                    }
                    dm.influirScore.movieScore = (float) ((0.4) * dm.influirScore.influScore / (infMaxA == 0 ? 1 : infMaxA)) + (float) ((0.15) * (dm.influirScore.genreScore * 100 / gSize)) + (float) ((0.05) * (dm.influirScore.keywordScore * 100 / kSize)) + (float) ((0.2) * dm.influirScore.criticScore) + (float) ((0.2) * dm.influirScore.audienceScore);
                    int score = (int) (dm.influirScore.movieScore * 100);
                    dm.influirScore.movieScore = ((float) score) / 100;
                }
            }

        }
        // Collections.sort(Inf_Lvl2, new MyIntComparable());

        //Compute statistics

        //Compare Genre






    }

    private void getInfDirectors(Influir.QueryType operation, String sparqlQuery, ArrayList<String> directors, HashMap<String, String> dirMap, String director) throws QueryEvaluationException, RepositoryException, MalformedQueryException
    {
        TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
        TupleQueryResult result = query.evaluate();
        while (result.hasNext())
        {

            BindingSet bindingSet = result.next();
            for (String bindingName : bindingSet.getBindingNames())
            {
                if ("director1".equals(bindingName))
                {
                    directors.add(bindingSet.getValue(bindingName).stringValue());
                    if (dirMap != null)
                    {
                        dirMap.put(bindingSet.getValue(bindingName).stringValue(), director);
                    }
                }
            }
        }
    }

    class Influence
    {
        public HashMap<String, ArrayList<String>> director_InfluencedBy_Lvl2;
        public HashMap<String, ArrayList<String>> influencedBy_Director_Lvl2;
        public int score;

        /*@Override
         public int compareTo(Influence director) {

         int compareQuantity = ((Influence) director).score;

         return compareQuantity - this.score;

         }*/
    }

    public class MyIntComparable implements Comparator<Influence>
    {
        @Override
        public int compare(Influence ob1, Influence ob2)
        {
            int o1 = ob1.score, o2 = ob2.score;
            return (o1 > o2 ? -1 : (o1 == o2 ? 0 : 1));
        }
    }
}
