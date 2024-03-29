package com.test.salesportal.dao.jpa;

import java.util.List;

import javax.persistence.Query;

import com.test.salesportal.dao.CVStorageException;
import com.test.salesportal.dao.ICVDAO;
import com.test.salesportal.model.cv.CV;
import com.test.salesportal.model.cv.CVItem;
import com.test.salesportal.model.cv.Custom;
import com.test.salesportal.model.cv.DescribedItem;
import com.test.salesportal.model.cv.Education;
import com.test.salesportal.model.cv.ItemVisitor;
import com.test.salesportal.model.cv.Job;
import com.test.salesportal.model.cv.Project;
import com.test.salesportal.model.cv.SelfEmployed;
import com.test.salesportal.model.cv.Skill;
import com.test.salesportal.model.cv.SkillCategory;
import com.test.salesportal.model.cv.SkillsAquiringItem;
import com.test.salesportal.model.cv.Work;
import com.test.salesportal.model.text.Language;
import com.test.salesportal.model.text.Text;
import com.test.salesportal.model.text.Translation;

public class JPACVDAO extends JPABaseDAO implements ICVDAO {
	
	public JPACVDAO(String persistenceUnitName) {
		super(persistenceUnitName);
	}

	private CV queryCV(String userId) {
		final Query query = entityManager.createQuery("from CV cv where cv.user.userId = :userId");
		
		query.setParameter("userId", userId);
		
		final CV cv = (CV)query.getSingleResult();

		return cv;
	}
	
	@Override
	public CV findCV(String userId, Language... languages) {

		final CV cv = queryCV(userId);
		
		if (cv != null) {
			// Filter languages for all fields that have multiple so that we only get at most one text
			filterLanguages(cv, languages);
		}

		return cv;
	}
	
	
	@Override
	public CV findCVForEdit(String userId) throws CVStorageException {
		return queryCV(userId);
	}

	@Override
	public void createCV(String userId, CV cv) {
		entityManager.persist(cv); // cascades
	}

	@Override
	public void updateCV(String userId, CV cv) {
		entityManager.persist(cv); // cascades
	}

	@Override
	public void deleteCV(String userId) {
		final CV cv = queryCV(userId);
		
		if (cv != null) {
			entityManager.remove(cv); // cascades
		}
	}

	private static void filterTexts(Text texts, Language [] languages) {
		filterTexts(texts.getTexts(), languages);
	}

	private static void filterTexts(List<Translation> texts, Language [] languages) {
		// Loop over languages and find first one that match
		Translation found = null;
		
		for (Language language : languages) {
			for (Translation text : texts) {
				if (language.equals(text.getLanguage())) {
					found = text;
					break;
				}
			}
			
			if (found != null) {
				final Translation f = found;
				// found matching text, remove all other items from list
				texts.removeIf(t -> t != f);
				break;
			}
		}
	}

	private static void filterDescribedItem(DescribedItem item, Language [] languages) {
		if (item.getSummary() != null) {
			filterTexts(item.getSummary(), languages);
		}

		if (item.getDescription() != null) {
			filterTexts(item.getDescription(), languages);
		}
	}

	private static void filterSkillAquirngItem(SkillsAquiringItem skillAquiringItem, Language [] languages) {
		
		filterDescribedItem(skillAquiringItem, languages);
		
		if (skillAquiringItem.getSkills() != null) {
			for (Skill skill : skillAquiringItem.getSkills()) {
				filterSkill(skill, languages);
			}
		}
	}

	private static void filterWork(Work work, Language [] languages) {
		filterSkillAquirngItem(work, languages);
	}

	private static void filterProject(Project project, Language [] languages) {
		filterSkillAquirngItem(project, languages);
	}

	private static void filterSkill(Skill skill, Language [] languages) {
		if (skill.getName() != null) {
			filterTexts(skill.getName(), languages);
		}
		
		if (skill.getDescription() != null) {
			filterTexts(skill.getDescription(), languages);
		}
		
		if (skill.getCategories() != null) {
			for (SkillCategory category : skill.getCategories()) {
				filterSkillCategory(category, languages);
			}
		}
	}
	
	private static void filterSkillCategory(SkillCategory category, Language [] languages) {
		if (category.getName() != null) {
			filterTexts(category.getName(), languages);
		}
		
		if (category.getDescription() != null) {
			filterTexts(category.getDescription(), languages);
		}
	}
	
	private static final ItemVisitor<Language[], Void> filterLanguagesVisitor = new ItemVisitor<Language[], Void>() {

		@Override
		public Void onJob(Job job, Language[] param) {
			filterWork(job, param);
			
			if (job.getPosition() != null) {
				filterTexts(job.getPosition(), param);
			}
			
			return null;
		}

		@Override
		public Void onProject(Project project, Language[] param) {

			filterProject(project, param);
			
			return null;
		}

		@Override
		public Void onSelfEmployed(SelfEmployed selfEmployed, Language[] param) {
			filterWork(selfEmployed, param);
			
			return null;
		}

		@Override
		public Void onEducation(Education education, Language[] param) {

			return null;
		}

		@Override
		public Void onCustom(Custom custom, Language[] param) {

			filterDescribedItem(custom, param);
		
			if (custom.getName() != null) {
				filterTexts(custom.getName(), param);
			}

			return null;
		}
	};
	
	private static void filterLanguages(CV cv, Language [] languages) {
		if (cv.getItems() != null) {
			for (CVItem item : cv.getItems()) {
				filterTexts(item.getExitReason(), languages);
				
				item.visit(filterLanguagesVisitor, languages);
			}
		}
	}
}
