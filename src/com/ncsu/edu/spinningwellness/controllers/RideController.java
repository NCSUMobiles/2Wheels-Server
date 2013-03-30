package com.ncsu.edu.spinningwellness.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.ncsu.edu.spinningwellness.entities.Participant;
import com.ncsu.edu.spinningwellness.entities.Ride;
import com.ncsu.edu.spinningwellness.utils.RideUtils;
import com.ncsu.edu.spinningwellness.utils.UserUtils;
import com.ncsu.edu.spinningwellness.utils.Utils;

@Path("/l2wride/")
public class RideController {

	/**
	 * create ride
	 * upString ride
	 * delete ride
	 * view ride
	 * 
	 * view past rides from last week
	 * view past rides
	 * 
	 * view upcoming rides from next week
	 * view upcoming rides
	 * 
	 * add participant to the ride
	 * delete participant to the ride
	 * view participants for a ride
	 * 
	 * view past rides for a user
	 * view past rides from last week for a user
	 * 
	 * view upcoming rides for a user
	 * view upcoming rides for a user for next week
	 * 
	 */

	/**
	 * Creates a ride in datastore.
	 * Before creating the ride, a check is added to make sure that the ride with same id is not present in the datastore already.
	 * After the ride is successfully created, an entry for ride, creator is added in participants table
	 *
	 * @param  	ride 	the ride object which is to be persisted in the datastore.
	 * 
	 * @return			a string stating the status of the operation either success or failure with appropriate message.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/createride") 
	public String createRide(Ride ride){

		Entity persistedRide = RideUtils.getSingleRide(ride.getId());
		if(persistedRide != null) {
			return "Failure: Duplicate ride id";
		} else {

			Entity persistedUser = UserUtils.getSingleUser(ride.getCreator());
			if(persistedUser != null) {			
				DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

				Entity dbRide = new Entity("Ride", ride.getId());
				dbRide.setProperty("id", ride.getId());
				dbRide.setProperty("name", ride.getName());
				dbRide.setProperty("source", ride.getSource());
				dbRide.setProperty("destination", ride.getDest());
				dbRide.setProperty("creator", persistedUser.getKey());
				dbRide.setProperty("startTime", ride.getStartTime());

				ds.put(dbRide);
				
				addParticipantToRide(new Participant(((Long)(Long.parseLong(ride.getId())+1)).toString(), ride.getId(), ride.getCreator()));

				return "Success";
			} else {
				return "Failure: Creator does not exist";	
			}
		}
	}

	/**
	 * UpStrings the ride given by the provided id.
	 * Before updating the ride, a check is made to make sure that the ride actually exists in the datastore before updating its parameters.
	 *
	 * @param  	id  	id of the ride which is to be upStringd.
	 * @param	ride	a new ride object which is to be used for replacing the old ride entity given by id.
	 * @return			a string stating the status of the operation either success or failure with appropriate message.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/upStringride/{id}") 
	public String updateRide(@PathParam("id") String id, Ride ride){

		Entity persistedRide = RideUtils.getSingleRide(id);
		if(persistedRide == null) {
			return "Failure: Ride does not exist";
		} else {
			Entity persistedUser = UserUtils.getSingleUser(ride.getCreator());
			if(persistedUser != null) {			
				DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

				Entity dbRide = new Entity("Ride", id);
				dbRide.setProperty("id", id);
				dbRide.setProperty("name", ride.getName());
				dbRide.setProperty("source", ride.getSource());
				dbRide.setProperty("destination", ride.getDest());
				dbRide.setProperty("creator", persistedUser.getKey());
				dbRide.setProperty("startTime", ride.getStartTime());

				ds.put(dbRide);

				return "Success";
			} else {
				return "Failure: Invalid creator";	
			}
		}
	}

	/**
	 * Deletes the ride from data store.
	 * Before deleting the ride from database, the method deletes all the participants for the ride.
	 *
	 * @param  	id  	id of the ride which is to be deleted.
	 * 
	 * @return			a string stating the status of the operation either success or failure with appropriate message.
	 */
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/deleteride/{id}")	
	public String deleteRide(@PathParam("id") String id) {

		Entity persistedRide = RideUtils.getSingleRide(id);
		if(persistedRide == null) {
			return "Failure: Invalid ride id";
		} else {

			List<String> participants = RideUtils.getAllParticipantsByRideId(id);
			for(String participant : participants) {
				removeParticipantFromRide(id, participant);
			}

			DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
			Key rideKey = KeyFactory.createKey("Ride", id);
			ds.delete(rideKey);

			return "Success";
		}
	}

	/**
	 * Returns the ride from data store identified by id.
	 *
	 * @param  	id  		id of the ride which is to be returned.
	 * 
	 * @return				ride object from datastore identified by id.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/viewride/{id}")
	public Ride viewRide(@PathParam("id") String id) {

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Key rideKey = KeyFactory.createKey("Ride", id);
		try {
			Entity dbRide = ds.get(rideKey);

			String name = (String) dbRide.getProperty("name");
			String source = (String) dbRide.getProperty("source");
			String dest = (String) dbRide.getProperty("destination");
			Key creatorKey = (Key) dbRide.getProperty("creator");
			String creator = creatorKey.getName();
			long startTime = (Long) dbRide.getProperty("startTime");

			Ride ride = new Ride(id, name, source, dest, startTime, creator);
			return ride;
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns all the rides from datastore which have start time within last week.
	 *
	 * @return		a list of ride objects which have start time within last week.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/viewpastridesfromlastweek") 
	public List<Ride> viewPastRidesFromLastWeek() {
		List<Ride> rides = new ArrayList<Ride>();

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

		Date today = new Date();

		Query query = new Query("Ride");
		Filter startTimeFilterLessThan = new Query.FilterPredicate("startTime", FilterOperator.LESS_THAN_OR_EQUAL, Utils.convertDateToString(today));
		
		Calendar cal = Calendar.getInstance();  
		cal.setTime(today);  
		cal.add(Calendar.DATE, -7);  
		today = cal.getTime();  		
		Filter startTimeFilterGreaterThan = new Query.FilterPredicate("startTime", FilterOperator.GREATER_THAN_OR_EQUAL, Utils.convertDateToString(today));

		Filter activityStringRangeFilter = CompositeFilterOperator.and(startTimeFilterLessThan, startTimeFilterGreaterThan);
		query.setFilter(activityStringRangeFilter);

		List<Entity> results = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());
		for(Entity result : results) {

			String id = (String) result.getProperty("id");
			String name = (String) result.getProperty("name");
			String source = (String) result.getProperty("source");
			String dest = (String) result.getProperty("destination");
			Key creatorKey = (Key) result.getProperty("creator");
			String creator = creatorKey.getName();
			long startTime = (Long) result.getProperty("startTime");

			Ride r = new Ride(id, name, source, dest, startTime, creator);
			rides.add(r);
		}
		return rides;
	}

	/**
	 * Returns all the rides from datastore for which the start time is before current time.
	 *
	 * @return		a list of ride objects which have start time before current time.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/viewpastrides") 
	public List<Ride> viewPastRides() {

		List<Ride> rides = new ArrayList<Ride>();

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

		Date today = new Date();

		Query query = new Query("Ride");
		Filter startTimeFilter =
				new Query.FilterPredicate("startTime", FilterOperator.LESS_THAN_OR_EQUAL, Utils.convertDateToString(today));

		query.setFilter(startTimeFilter);
		List<Entity> results = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());

		for(Entity result : results) {

			String id = (String) result.getProperty("id");
			String name = (String) result.getProperty("name");
			String source = (String) result.getProperty("source");
			String dest = (String) result.getProperty("destination");
			Key creatorKey = (Key) result.getProperty("creator");
			String creator = creatorKey.getName();
			long startTime = (Long) result.getProperty("startTime");

			Ride r = new Ride(id, name, source, dest, startTime, creator);
			rides.add(r);
		}
		return rides;
	}

	/**
	 * Returns all the rides from datastore which have start time within next week.
	 *
	 * @return		a list of ride objects which have start time within next week.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/viewupcomingridesfromnextweek") 
	public List<Ride> viewUpcomingRidesFromNextWeek() {
		List<Ride> rides = new ArrayList<Ride>();

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

		Date today = new Date();

		Query query = new Query("Ride");
		Filter startTimeFilterGreaterThan = new Query.FilterPredicate("startTime", FilterOperator.GREATER_THAN_OR_EQUAL, Utils.convertDateToString(today));
		
		Calendar cal = Calendar.getInstance();  
		cal.setTime(today);
		cal.add(Calendar.DATE, 7);
		today = cal.getTime();
		Filter startTimeFilterLessThan = new Query.FilterPredicate("startTime", FilterOperator.LESS_THAN_OR_EQUAL, Utils.convertDateToString(today));

		Filter activityStringRangeFilter = CompositeFilterOperator.and(startTimeFilterLessThan, startTimeFilterGreaterThan);
		query.setFilter(activityStringRangeFilter);

		List<Entity> results = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());
		for(Entity result : results) {

			String id = (String) result.getProperty("id");
			String name = (String) result.getProperty("name");
			String source = (String) result.getProperty("source");
			String dest = (String) result.getProperty("destination");
			Key creatorKey = (Key) result.getProperty("creator");
			String creator = creatorKey.getName();
			long startTime = (Long) result.getProperty("startTime");

			Ride r = new Ride(id, name, source, dest, startTime, creator);
			rides.add(r);
		}
		return rides;
	}

	/**
	 * Returns all the rides from datastore for which the start time is after current time.
	 *
	 * @return		a list of ride objects which have start time after current time.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/viewupcomingrides") 
	public List<Ride> viewUpcomingRides() {
		List<Ride> rides = new ArrayList<Ride>();

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

		Date today = new Date();

		Query query = new Query("Ride");
		Filter startTimeFilter = new Query.FilterPredicate("startTime", FilterOperator.GREATER_THAN_OR_EQUAL, Utils.convertDateToString(today));

		query.setFilter(startTimeFilter);
		List<Entity> results = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());

		for(Entity result : results) {

			String id = (String) result.getProperty("id");
			String name = (String) result.getProperty("name");
			String source = (String) result.getProperty("source");
			String dest = (String) result.getProperty("destination");
			Key creatorKey = (Key) result.getProperty("creator");
			String creator = creatorKey.getName();
			long startTime = (Long) result.getProperty("startTime");

			Ride r = new Ride(id, name, source, dest, startTime, creator);
			rides.add(r);
		}
		return rides;
	}

	/**
	 * Creates a participant entity for a given ride using the given user name.
	 * A check is performed to make sure that the ride specified by given rideId and the user specified by given userName
	 * exists in the database.
	 * A check is performed to make sure that the same rideId, userName combination is not already registered as the participant. 
	 * Before deleting the ride from database, the method deletes all the participants for the ride.
	 *
	 * @param  participant  participant object which is to be persisted in the database.
	 * 
	 * @return				a string stating the status of the operation either success or failure with appropriate message.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/addparticipanttoride") 
	public String addParticipantToRide(Participant participant){

		Entity persistedRide = RideUtils.getSingleRide(participant.getRideId());
		if(persistedRide != null) {
			Entity persistedUser = UserUtils.getSingleUser(participant.getUserName());
			if(persistedUser != null) {

				Entity persistedParticipant = RideUtils.getSingleParticipant(participant.getRideId(), participant.getUserName());
				if(persistedParticipant != null) {
					return "Failure: Participant already exists";
				} else {
					DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

					Entity dbParticipant = new Entity("Participant");
					dbParticipant.setProperty("id", participant.getId());
					dbParticipant.setProperty("rideId", persistedRide.getKey());
					dbParticipant.setProperty("userName", persistedUser.getKey());

					ds.put(dbParticipant);

					return "Success";
				}
			} else {
				return "Failure: Invalid user name";
			}
		} else {
			return "Failure: Invalid ride id";
		}
	}

	/**
	 * Removes the participant from the ride.
	 * A check is performed to make sure that the given participant indicated by rideId and userName pair actually exists in the
	 * database before deleting it.
	 *
	 * @param  rideId  		id of the ride of the participant to be deleted.
	 * @param  userName  	userName of the participant to be deleted.
	 * 
	 * @return		a string stating the status of the operation either success or failure with appropriate message.
	 */
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/removeparticipanttoride/{id}/{name}")
	public String removeParticipantFromRide(@PathParam("id") String rideId, @PathParam("name") String userName){

		Entity persistedParticipant = RideUtils.getSingleParticipant(rideId, userName);
		if(persistedParticipant != null) {

				DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
				ds.delete(persistedParticipant.getKey());

				return "Success";
		} else {
			return "Failure: Invalid participant info";
		}
	}

	/**
	 * Returns all the participants for the ride.
	 *
	 * @return		a list of participant objects for a given ride.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/viewparticipantsforride/{id}") 
	public List<Participant> viewParticipantsForRide(@PathParam("id") String rideId) {
		List<Participant> participants = new ArrayList<Participant>();

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

		Query query = new Query("Participant");
		Filter rideIdFilter = new Query.FilterPredicate("rideId", FilterOperator.EQUAL, KeyFactory.createKey("Ride", rideId));
		query.setFilter(rideIdFilter);

		List<Entity> results = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());
		for(Entity result : results) {

			String id = (String) result.getProperty("id");
			String userName = ((Key) result.getProperty("userName")).getName();

			participants.add(new Participant(id, rideId, userName));
		}
		return participants;
	}
	
	/**
	 * Returns all the upcoming rides for a user from next week.
	 *
	 * @param  	name  	name of the user for which upcoming rides are requested.
	 * 
	 * @return			list of upcoming rides for a user from past week.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/myupcomingridesfornextweek/{name}") 
	public List<Ride> getMyUpcomingRidesForNextWeek(@PathParam("name") String name) {
		List<Ride> rides = new ArrayList<Ride>();

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

		Query query = new Query("UserActivity");

		//End date filter
		Date today = new Date();	  
		Filter activityDateFilterLessThan = new Query.FilterPredicate("activityDate", FilterOperator.GREATER_THAN_OR_EQUAL, today);

		//Start date filter
		Calendar cal = Calendar.getInstance();  
		cal.setTime(today);  
		cal.add(Calendar.DATE, 7);  
		today = cal.getTime();  		
		Filter activityDateFilterGreaterThan = new Query.FilterPredicate("activityDate", FilterOperator.LESS_THAN_OR_EQUAL, today);

		//user name filter
		Filter activityUserFilter = new Query.FilterPredicate("userName", FilterOperator.EQUAL, KeyFactory.createKey("User", name));

		//complete composite filter
		Filter activityFilter = CompositeFilterOperator.and(activityDateFilterLessThan, activityDateFilterGreaterThan, activityUserFilter);
		query.setFilter(activityFilter);

		List<Entity> results = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());		
		for(Entity result: results) {	
			String rideId = (String) result.getProperty("rideId");
			RideController rc = new RideController();
			rides.add(rc.viewRide(rideId));
		}		
		return rides;
	}

	/**
	 * Returns all the upcoming rides for a user.
	 *
	 * @param  	name  	name of the user for which upcoming rides are requested.
	 * 
	 * @return			list of upcoming rides for a user.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/myupcomingrides/{name}") 
	public List<Ride> getMyUpcomingRides(@PathParam("name") String name) {
		List<Ride> rides = new ArrayList<Ride>();

		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

		Query query = new Query("UserActivity");

		//End date filter
		Date today = new Date();	  
		Filter activityDateFilterLessThan = new Query.FilterPredicate("activityDate", FilterOperator.GREATER_THAN_OR_EQUAL, today);		

		//User name filter
		Filter activityUserFilter = new Query.FilterPredicate("userName", FilterOperator.EQUAL, KeyFactory.createKey("User", name));

		//complete composite filter
		Filter activityFilter = CompositeFilterOperator.and(activityDateFilterLessThan, activityUserFilter);
		query.setFilter(activityFilter);

		List<Entity> results = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());		
		for(Entity result: results) {	
			String rideId = (String) result.getProperty("rideId");
			RideController rc = new RideController();
			rides.add(rc.viewRide(rideId));
		}		
		return rides;
	}

	
}