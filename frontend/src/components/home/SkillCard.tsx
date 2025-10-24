interface SkillCardProps {
  skill: {
    title: string;
    description: string;
    author: string;
  };
}

const SkillCard = ({ skill }: SkillCardProps) => (
  <div className="p-6 bg-white rounded-2xl shadow-lg transition-transform transform hover:scale-[1.02] hover:-translate-y-1 cursor-pointer">
    <h3 className="text-xl font-bold text-gray-800">{skill.title}</h3>
    <p className="mt-2 text-gray-600">{skill.description}</p>
    <p className="mt-4 text-sm font-semibold text-indigo-600">Təklif edir: {skill.author}</p>
  </div>
);

export default SkillCard;
