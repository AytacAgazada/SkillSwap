import SkillCard from './SkillCard';

const skills = [
  { title: 'Frontend Development', description: 'React və Tailwind CSS ilə müasir veb tətbiqlərin hazırlanması.', author: 'user123' },
  { title: 'Backend Development', description: 'Node.js və Express ilə güclü API-lərin qurulması.', author: 'user456' },
  { title: 'UI/UX Design', description: 'Figma və Sketch istifadə edərək intuitiv interfeyslərin dizaynı.', author: 'user789' },
];

const Home = () => (
  <div className="min-h-screen bg-gray-100">
    <header className="py-6 bg-white shadow-md">
      <div className="container mx-auto px-4">
        <h1 className="text-4xl font-bold text-center text-gray-800">SkillSwap Bazarı</h1>
        <p className="mt-2 text-lg text-center text-gray-600">Bacarıqlarınızı paylaşın və ya yenilərini öyrənin</p>
      </div>
    </header>
    <main className="container mx-auto px-4 py-10">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {skills.map((skill, index) => (
          <SkillCard key={index} skill={skill} />
        ))}
      </div>
    </main>
  </div>
);

export default Home;
