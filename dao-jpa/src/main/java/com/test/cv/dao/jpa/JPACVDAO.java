package com.test.cv.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.test.cv.dao.ICVDAO;
import com.test.cv.model.cv.CV;
import com.test.cv.model.cv.Custom;
import com.test.cv.model.cv.DescribedItem;
import com.test.cv.model.cv.Education;
import com.test.cv.model.cv.Item;
import com.test.cv.model.cv.ItemVisitor;
import com.test.cv.model.cv.Job;
import com.test.cv.model.cv.Language;
import com.test.cv.model.cv.Project;
import com.test.cv.model.cv.SelfEmployed;
import com.test.cv.model.cv.Skill;
import com.test.cv.model.cv.SkillCategory;
import com.test.cv.model.cv.SkillsAquiringItem;
import com.test.cv.model.cv.Text;
import com.test.cv.model.cv.Work;

public class JPACVDAO implements ICVDAO {
	
	private final EntityManagerFactory entityManagerFactory;
	private final EntityManager entityManager;
	
	public JPACVDAO(String persistenceUnitName) {
		this.entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
		this.entityManager = entityManagerFactory.createEntityManager();
	}

	@Override
	public void close() throws Exception {
		entityManager.close();
		entityManagerFactory.close();
	}

	@Override
	public CV findCV(String userId, Language... languages) {

		final Query query = entityManager.createQuery("from CV cv where cv.user.userId = :userId");
		
		query.setParameter("userId", userId);
		
		final CV cv = (CV)query.getSingleResult();
		
		if (cv != null) {
			// Filter languages for all fields that have multiple so that we only get at most one text
			filterLanguages(cv, languages);
		}

		return cv;
	}
	
	private static void filterTexts(List<Text> texts, Language [] languages) {
		// Loop over languages and find first one that match
		Text found = null;
		
		for (Language language : languages) {
			for (Text text : texts) {
				if (language.equals(text.getLanguage())) {
					found = text;
					break;
				}
			}
			
			if (found != null) {
				final Text f = found;
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
			for (Item item : cv.getItems()) {
				filterTexts(item.getExitReason(), languages);
				
				item.visit(filterLanguagesVisitor, languages);
			}
		}
	}
}
