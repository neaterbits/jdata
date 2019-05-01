package com.test.salesportal.dao.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.xml.datatype.XMLGregorianCalendar;

import com.test.salesportal.model.cv.CV;
import com.test.salesportal.model.cv.CVItem;
import com.test.salesportal.model.cv.Custom;
import com.test.salesportal.model.cv.DescribedItem;
import com.test.salesportal.model.cv.Education;
import com.test.salesportal.model.cv.ItemVisitor;
import com.test.salesportal.model.cv.Job;
import com.test.salesportal.model.cv.Name;
import com.test.salesportal.model.cv.Personalia;
import com.test.salesportal.model.cv.Project;
import com.test.salesportal.model.cv.SelfEmployed;
import com.test.salesportal.model.cv.Skill;
import com.test.salesportal.model.cv.SkillCategory;
import com.test.salesportal.model.cv.SkillsAquiringItem;
import com.test.salesportal.model.cv.Work;
import com.test.salesportal.model.text.Text;
import com.test.salesportal.model.text.Translation;
import com.test.salesportal.xml.CVType;
import com.test.salesportal.xml.CustomType;
import com.test.salesportal.xml.DescribedItemType;
import com.test.salesportal.xml.DescriptionType;
import com.test.salesportal.xml.EducationType;
import com.test.salesportal.xml.ExitReasonType;
import com.test.salesportal.xml.ItemType;
import com.test.salesportal.xml.ItemsType;
import com.test.salesportal.xml.JobType;
import com.test.salesportal.xml.NameType;
import com.test.salesportal.xml.PersonaliaType;
import com.test.salesportal.xml.PositionType;
import com.test.salesportal.xml.ProjectType;
import com.test.salesportal.xml.SelfEmployedType;
import com.test.salesportal.xml.SkillCategoriesType;
import com.test.salesportal.xml.SkillCategoryType;
import com.test.salesportal.xml.SkillRefType;
import com.test.salesportal.xml.SkillRefsType;
import com.test.salesportal.xml.SkillType;
import com.test.salesportal.xml.SkillsAquiringItemType;
import com.test.salesportal.xml.SkillsType;
import com.test.salesportal.xml.SummaryType;
import com.test.salesportal.xml.TextType;
import com.test.salesportal.xml.WorkType;

public class ConvertModelToXML {
	
	private static <T> void add(Supplier<List<T>> getList, T item) {
		if (item == null) {
			throw new IllegalArgumentException("item == null");
		}
		
		List<T> l = getList.get();
		
		if (l == null) {
			throw new IllegalStateException("XML type should return non-null");
		}

		if (l.contains(item)) {
			throw new IllegalStateException("Already contains item " + item);
		}

		l.add(item);
	}
	
	
	private static ItemVisitor<ItemsType, ItemType> modelToXmlItemVisitor = new ItemVisitor<ItemsType, ItemType>() {
		
		@Override
		public ItemType onSelfEmployed(SelfEmployed selfEmployed, ItemsType param) {

			final SelfEmployedType xmlItem = convertSelfEmployed(selfEmployed);
			
			add(param::getSelfEmployed, xmlItem);
			
			return xmlItem;
		}
		
		@Override
		public ItemType onProject(Project project, ItemsType param) {
			
			final ProjectType xmlItem = convertProject(project);
			
			add(param::getProject, xmlItem);

			return xmlItem;
		}
		
		@Override
		public ItemType onJob(Job job, ItemsType param) {

			final JobType xmlItem = convertJob(job);
			
			add(param::getJob,xmlItem);
			
			return xmlItem;
		}
		
		@Override
		public ItemType onEducation(Education education, ItemsType param) {
			
			final EducationType xmlItem = convertEducation(education);
			
			add(param::getEducation, xmlItem);
			
			return xmlItem;
		}
		
		@Override
		public ItemType onCustom(Custom custom, ItemsType param) {

			final CustomType xmlItem = convertCustom(custom);
			
			add(param::getCustom, xmlItem);
			
			return xmlItem;
		}
	};
	
	static CVType convertToXML(CV modelCV) {
		
		final CVType ret = new CVType();
		
		if (modelCV.getPersonalia() != null) {
			ret.setPersonalia(convertPersonalia(modelCV.getPersonalia()));
		}

		
		final List<SkillType> xmlSkills;
		final List<SkillCategoryType> xmlSkillCategories;
		
		// Must find all skills and reference them by ID instead, in order to avoid duplication in XML
		if (modelCV.getItems() != null) {

			final Map<Long, Skill> distinctSkills = new HashMap<>();
			final Map<String, SkillCategory> distinctSkillCategories = new HashMap<>();

			for (CVItem item : modelCV.getItems()) {
				
				if (item instanceof SkillsAquiringItem) {
					// Can aquire skills
					final SkillsAquiringItem skillsAquiringItem = (SkillsAquiringItem)item;

					addDistinctSkills(skillsAquiringItem, distinctSkills, distinctSkillCategories);
				}
				
				if (item instanceof Work) {
					final Work work = (Work)item;
					
					if (work.getProjects() != null) {
						for (Project project : work.getProjects()) {
							addDistinctSkills(project, distinctSkills, distinctSkillCategories);
						}
					}
				}
			}

			if (distinctSkills.isEmpty()) {
				xmlSkills = null;
				xmlSkillCategories = null;
			}
			else {
				xmlSkills = new ArrayList<>(distinctSkills.size());
				
				for (Skill modelSkill : distinctSkills.values()) {
					xmlSkills.add(convertSkill(modelSkill));
				}
				
				xmlSkillCategories = new ArrayList<>(distinctSkillCategories.size());
				
				for (SkillCategory modelSkillCategory : distinctSkillCategories.values()) {
					xmlSkillCategories.add(convertSkillCategory(modelSkillCategory));
				}
			}
		}
		else {
			xmlSkills = null;
			xmlSkillCategories = null;
		}

		if (xmlSkills != null) {
			
			final SkillsType skillsType = new SkillsType();
			
			skillsType.getSkills().addAll(xmlSkills);
			
			ret.setSkills(skillsType);
		}

		if (xmlSkillCategories != null) {
			
			final SkillCategoriesType categoriesType = new SkillCategoriesType();
			
			categoriesType.getCategory().addAll(xmlSkillCategories);
			
			ret.setSkillategories(categoriesType);
		}

		
		if (modelCV.getItems() != null) {

			final ItemsType xmlItems = new ItemsType();

			ret.setItems(xmlItems);

			for (CVItem item : modelCV.getItems()) {
				item.visit(modelToXmlItemVisitor, xmlItems);
			}
		}
		
		return ret;
	}

	private static void addDistinctSkills(
			SkillsAquiringItem skillsAquiringItem,
			Map<Long, Skill> distinctSkills,
			Map<String, SkillCategory> distinctSkillCategories) {
		
		if (skillsAquiringItem.getSkills() != null) {

			for (Skill skill : skillsAquiringItem.getSkills()) {

				// make sure ID is set to something sensible
				if (skill.getId() <= 0) {
					throw new IllegalStateException("skill.getId() <= 0");
				}

				if (!distinctSkills.containsKey(skill.getId())) {
					distinctSkills.put(skill.getId(), skill);
				}
				
				if (skill.getCategories() != null) {
					for (SkillCategory skillCategory : skill.getCategories()) {
						final String categoryId = skillCategory.getSkillCategoryId();
						if (!distinctSkillCategories.containsKey(categoryId)) {
							distinctSkillCategories.put(categoryId, skillCategory);
						}
					}
				}
			}
		}
	}
	

	private static List<TextType> convertTexts(Text modelTexts) {
		
		final List<TextType> ret;
		
		if (modelTexts.getTexts() != null && !modelTexts.getTexts().isEmpty()) {
			ret = new ArrayList<>(modelTexts.getTexts().size());

			// No languages, just return all texts
			for (Translation modelText : modelTexts.getTexts()) {
				final TextType text = convertText(modelText);
				
				ret.add(text);
			}
		}
		else {
			ret = Collections.emptyList();
		}

		return ret;
	}
	
	private static TextType convertText(Translation modelText) {
		final TextType text = new TextType();
		
		if (modelText.getLanguage() != null) {
			text.setLanguage(modelText.getLanguage().getCode());
		}

		text.setText(modelText.getText());

		return text;
	}

	private static NameType convertName(Name modelName) {
		
		final NameType name = new NameType();
		
		name.getText().addAll(convertTexts(modelName));
		
		return name;
	}

	private static ExitReasonType convertExitReason(Text modelExitReason) {

		final ExitReasonType ret = new ExitReasonType();

		ret.getText().addAll(convertTexts(modelExitReason));

		return ret;
	}

	private static PositionType convertPosition(Text modelPosition) {

		final PositionType ret = new PositionType();

		ret.getText().addAll(convertTexts(modelPosition));

		return ret;
	}

	private static SummaryType convertSummary(Text modelSummary) {
		
		final SummaryType summary = new SummaryType();
		
		summary.getText().addAll(convertTexts(modelSummary));
		
		return summary;
	}

	private static DescriptionType convertDescription(Text modelDescription) {
		
		final DescriptionType description = new DescriptionType();
		
		description.getText().addAll(convertTexts(modelDescription));
		
		return description;
	}

	private static SelfEmployedType convertSelfEmployed(SelfEmployed modelSelfEmployed) {
		
		final SelfEmployedType ret = new SelfEmployedType();

		convertWork(modelSelfEmployed, ret);

		return ret;
	}

	private static ProjectType convertProject(Project modelProject) {
		
		final ProjectType ret = new ProjectType();

		convertSkillAquiringItem(modelProject, ret);

		return ret;
	}

	private static EducationType convertEducation(Education modelEducation) {
		
		final EducationType ret = new EducationType();
		
		convertItem(modelEducation, ret);
		
		return ret;
	}

	private static CustomType convertCustom(Custom modelCustom) {
		
		final CustomType ret = new CustomType();
		
		if (modelCustom.getName() != null) {
			ret.setName(convertName(modelCustom.getName()));
		}
		
		return ret;
	}
	
	private static PersonaliaType convertPersonalia(Personalia modelPersonalia) {
		final PersonaliaType ret = new PersonaliaType();

		ret.setFirstName(modelPersonalia.getFirstName());
		ret.setLastName(modelPersonalia.getLastName());
		ret.setEmailAddress(modelPersonalia.getEmailAddress());
		ret.setDateOfBirth(convertCalendar(modelPersonalia.getDateOfBirth()));
		
		return ret;
	}

	private static void convertItem(CVItem modelItem, ItemType ret) {
		ret.setStartTime(convertCalendar(modelItem.getStartTime()));
		ret.setEndTime(convertCalendar(modelItem.getEndTime()));
		
		if (modelItem.getExitReason() != null) {
			ret.setExitReason(convertExitReason(modelItem.getExitReason()));
		}
	}
	
	private static XMLGregorianCalendar convertCalendar(Date modelCalendar) {
		
		/* TODO
		final XMLGregorianCalendar xmlCalendar = new XmlGregorianCalendar();
		
		xmlCalendar.setMillisecond(modelCalendar.getTime());

		return xmlCalendar;
		*/
		throw new UnsupportedOperationException("TODO");
	}
	
	private static SkillType convertSkill(Skill modelSkill) {
		final SkillType ret = new SkillType();
		
		if (modelSkill.getName() != null) {
			ret.setName(convertName(modelSkill.getName()));
		}
		
		if (modelSkill.getDescription() != null) {
			ret.setDescription(convertDescription(modelSkill.getDescription()));
		}
		
		return ret;
	}
	
	private static SkillCategoryType convertSkillCategory(SkillCategory modelSkillCategory) {
		final SkillCategoryType ret = new SkillCategoryType();
		
		ret.setId(modelSkillCategory.getSkillCategoryId());
		
		if (modelSkillCategory.getName() != null) {
			ret.setName(convertName(modelSkillCategory.getName()));
		}
	
		if (modelSkillCategory.getDescription() != null) {
			ret.setDescription(convertDescription(modelSkillCategory.getDescription()));
		}
		
		return ret;
	}

	private static SkillRefsType convertSkillRefs(List<Skill> skills) {
		
		final List<SkillRefType> ret;
		
		if (skills != null && !skills.isEmpty()) {
			ret = new ArrayList<>(skills.size());
			
			// For each skill ref, find corresponding skill
			for (Skill modelSkill : skills) {
				final SkillRefType skillRef = new SkillRefType();
				
				skillRef.setId((int)modelSkill.getId());

				ret.add(skillRef);
			}
		}
		else {
			ret = Collections.emptyList();
		}
		
		final SkillRefsType refs = new SkillRefsType();
		
		refs.getSkill().addAll(ret);
		
		return refs;
	}
	
	private static void convertDescribedItem(DescribedItem modelItem, DescribedItemType ret) {
		convertItem(modelItem, ret);
	}
	
	private static void convertSkillAquiringItem(SkillsAquiringItem modelItem, SkillsAquiringItemType ret) {

		convertDescribedItem(modelItem, ret);
		
		if (modelItem.getSkills() != null && modelItem.getSkills() != null && !modelItem.getSkills().isEmpty()) {
			ret.setSkills(convertSkillRefs(modelItem.getSkills()));
		}
	}
	
	
	private static void convertWork(Work modelWork, WorkType ret) {
		convertSkillAquiringItem(modelWork, ret);

		if (modelWork.getSummary() != null) {
			ret.setSummary(convertSummary(modelWork.getSummary()));
		}
		
		if (modelWork.getDescription() != null) {
			ret.setDescription(convertDescription(modelWork.getDescription()));
		}
		
	}

	private static JobType convertJob(Job modelJob) {
		final JobType ret = new JobType();
		
		convertWork(modelJob, ret);
		
		if (modelJob.getEmployerName() != null) {
			ret.setEmployerName(modelJob.getEmployerName());
		}
		
		if (modelJob.getPosition() != null) {
			ret.setPosition(convertPosition(modelJob.getPosition()));
		}
		
		return ret;
	}

}
