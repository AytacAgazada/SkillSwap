import { Link, Outlet } from 'react-router-dom';

const Layout = () => (
  <div>
    <nav className="bg-white shadow-lg">
      <div className="container mx-auto px-6 py-4 flex justify-between items-center">
        <Link to="/" className="text-2xl font-bold text-indigo-600">SkillSwap</Link>
        <div>
          <Link to="/login" className="px-4 py-2 text-sm font-semibold text-gray-700 rounded-xl hover:bg-gray-200 transition-colors">Daxil Ol</Link>
          <Link to="/register" className="ml-2 px-4 py-2 text-sm font-semibold text-white bg-indigo-600 rounded-xl hover:bg-indigo-700 transition-colors">Qeydiyyat</Link>
        </div>
      </div>
    </nav>
    <main>
      <Outlet />
    </main>
  </div>
);

export default Layout;
