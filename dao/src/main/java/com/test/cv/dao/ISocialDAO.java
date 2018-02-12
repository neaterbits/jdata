package com.test.cv.dao;

import java.util.Date;
import java.util.List;

import com.test.cv.model.cv.SkillsAquiringItem;
import com.test.cv.model.social.AcquaintanceType;
import com.test.cv.model.social.RecommendationDirection;
import com.test.cv.model.social.UserToUserRelation;
import com.test.cv.model.text.Text;

public interface ISocialDAO {

	void addUserToUserRelation(String firstUser, String secondUser);
	
	void removeUserToUserRelation(String firstUser, String secondUser);

	void setUserToUserRecommendation(String firstUser, String secondUser, RecommendationDirection direction, Text text);

	void removeUserToUserRecommendation(String firstUser, String secondUser, RecommendationDirection direction, Text text);

	List<String> getUserRelationsForUser(String user);

	List<UserToUserRelation> getUserRelationsForUserWithFacetsAndRecommendations(String user);

	void addUserToUserProfessionalRelationFacet(String firstUser, String secondUser, SkillsAquiringItem place, Date from, Date to);

	void removeUserToUserProfessionalRelationFacet(String firstUser, String secondUser, SkillsAquiringItem place);

	void addUserToUserAquaintanceRelationFacet(String firstUser, String secondUser, AcquaintanceType type, Date from, Date to);
	
	void removeUserToUserAquaintanceRelationFacet(String firstUser, String secondUser, AcquaintanceType type);
	
	void setUserToUserRelationFacetRecommendation(String firstUser, String secondUser, long facetId, RecommendationDirection direction, Text text);

	void removeUserToUserRelationFacetRecommendation(String firstUser, String secondUser, long facetId, RecommendationDirection direction, Text text);
	
}
