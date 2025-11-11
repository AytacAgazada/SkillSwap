import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

export type Language = 'az' | 'tr' | 'en' | 'ru';

interface Translations {
  [key: string]: {
    az: string;
    tr: string;
    en: string;
    ru: string;
  };
}

const translations: Translations = {
  home: { az: 'Ana Səhifə', tr: 'Ana Sayfa', en: 'Home', ru: 'Главная' },
  login: { az: 'Giriş', tr: 'Giriş', en: 'Login', ru: 'Вход' },
  signup: { az: 'Qeydiyyat', tr: 'Kayıt', en: 'Sign Up', ru: 'Регистрация' },
  logout: { az: 'Çıxış', tr: 'Çıkış', en: 'Logout', ru: 'Выход' },
  dashboard: { az: 'Panel', tr: 'Panel', en: 'Dashboard', ru: 'Панель' },
  profile: { az: 'Profil', tr: 'Profil', en: 'Profile', ru: 'Профиль' },
  welcome: { az: 'Xoş Gəlmisiniz', tr: 'Hoş Geldiniz', en: 'Welcome', ru: 'Добро пожаловать' },
  exchanges: { az: 'Mübadilələr', tr: 'Değişimler', en: 'Exchanges', ru: 'Обмены' },
  messages: { az: 'Mesajlar', tr: 'Mesajlar', en: 'Messages', ru: 'Сообщения' },
  groups: { az: 'Qruplar', tr: 'Gruplar', en: 'Groups', ru: 'Группы' },
  gamification: { az: 'Gamifikasiya', tr: 'Oyunlaştırma', en: 'Gamification', ru: 'Геймификация' },
  // Add more translations as needed
};

interface LanguageContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: string) => string;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export const useLanguage = () => {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
};

interface LanguageProviderProps {
  children: ReactNode;
}

export const LanguageProvider: React.FC<LanguageProviderProps> = ({ children }) => {
  const [language, setLanguage] = useState<Language>(() => {
    const saved = localStorage.getItem('language');
    return (saved as Language) || 'az';
  });

  useEffect(() => {
    localStorage.setItem('language', language);
  }, [language]);

  const t = (key: string): string => {
    return translations[key]?.[language] || key;
  };

  return (
    <LanguageContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
};

